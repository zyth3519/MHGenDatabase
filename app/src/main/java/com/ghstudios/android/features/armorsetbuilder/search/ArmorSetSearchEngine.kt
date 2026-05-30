package com.ghstudios.android.features.armorsetbuilder.search

import android.os.Handler
import android.os.Looper
import com.ghstudios.android.data.DataManager
import com.ghstudios.android.data.classes.*

data class SearchParams(
    val weaponSlots: Int = 0,
    val talisman: TalismanSpec? = null,
    val hunterType: Int = Armor.ARMOR_TYPE_BLADEMASTER,
    val requiredSkills: Map<Long, Int> = emptyMap(),
    val minDefense: Int = 0,
    val maxResults: Int = 100,
    val pinnedArmor: Map<String, Long> = emptyMap(),
    val excludedArmor: Set<Long> = emptySet()
)

data class TalismanSpec(
    val skill1Id: Long = -1, val skill1Points: Int = 0,
    val skill2Id: Long = -1, val skill2Points: Int = 0,
    val slots: Int = 0
)

data class ArmorSearchResult(
    val headId: Long, val bodyId: Long, val armsId: Long,
    val waistId: Long, val legsId: Long,
    val totalDefense: Int, val totalSlots: Int,
    val skillTotals: Map<Long, Int>,
    val matchedSkills: Int, val totalMatchedPoints: Int
)

private data class ArmorData(
    val id: Long, val slot: String, val defense: Int,
    val numSlots: Int, val hunterType: Int,
    val skills: List<Pair<Long, Int>>
)

class ArmorSetSearchEngine(
    private val dataManager: DataManager = DataManager.get()
) {
    private var allArmor: List<ArmorData>? = null
    @Volatile private var cancelled = false
    private val mainHandler = Handler(Looper.getMainLooper())

    fun cancelSearch() { cancelled = true }

    fun search(params: SearchParams,
               onProgress: (Int) -> Unit = {},
               onResult: (List<ArmorSearchResult>) -> Unit) {
        cancelled = false
        Thread {
            val results = doSearch(params) { pct ->
                mainHandler.post { onProgress(pct) }
            }
            mainHandler.post {
                if (!cancelled) onResult(results)
            }
        }.start()
    }

    private fun doSearch(params: SearchParams, onProgress: (Int) -> Unit): List<ArmorSearchResult> {
        val armorBySlot = loadArmorBySlot(params.hunterType)

        val slots = listOf(
            params.pinnedArmor["head"]?.let { listOfNotNull(armorById(it)) } ?: armorBySlot["head"].orEmpty(),
            params.pinnedArmor["body"]?.let { listOfNotNull(armorById(it)) } ?: armorBySlot["body"].orEmpty(),
            params.pinnedArmor["arms"]?.let { listOfNotNull(armorById(it)) } ?: armorBySlot["arms"].orEmpty(),
            params.pinnedArmor["waist"]?.let { listOfNotNull(armorById(it)) } ?: armorBySlot["waist"].orEmpty(),
            params.pinnedArmor["legs"]?.let { listOfNotNull(armorById(it)) } ?: armorBySlot["legs"].orEmpty()
        ).map { list -> list.filter { it.id !in params.excludedArmor } }

        if (slots.any { it.isEmpty() }) return emptyList()

        val totalCombos = slots.fold(1L) { acc, list -> acc * list.size }
        val results = mutableListOf<ArmorSearchResult>()
        var checked = 0L

        for (head in slots[0]) {
            if (cancelled) break
            for (body in slots[1]) {
                for (arms in slots[2]) {
                    for (waist in slots[3]) {
                        for (legs in slots[4]) {
                            if (cancelled) break
                            checked++
                            val r = score(head, body, arms, waist, legs,
                                params.weaponSlots, params.talisman,
                                params.requiredSkills, params.minDefense)
                            if (r != null) results.add(r)
                            if (checked % 1000 == 0L && totalCombos > 0) {
                                onProgress(((checked * 100) / totalCombos).toInt().coerceAtMost(99))
                            }
                        }
                    }
                }
            }
        }
        onProgress(100)

        results.sortWith(compareByDescending<ArmorSearchResult> { it.matchedSkills }
            .thenByDescending { it.totalMatchedPoints }
            .thenByDescending { it.totalDefense })

        return results.take(params.maxResults)
    }

    private fun score(
        head: ArmorData, body: ArmorData, arms: ArmorData,
        waist: ArmorData, legs: ArmorData,
        weaponSlots: Int, talisman: TalismanSpec?,
        requiredSkills: Map<Long, Int>, minDefense: Int
    ): ArmorSearchResult? {
        val totals = mutableMapOf<Long, Int>()
        for (p in listOf(head, body, arms, waist, legs))
            for ((sk, pts) in p.skills) totals[sk] = (totals[sk] ?: 0) + pts

        talisman?.let { t ->
            if (t.skill1Id > 0) totals[t.skill1Id] = (totals[t.skill1Id] ?: 0) + t.skill1Points
            if (t.skill2Id > 0) totals[t.skill2Id] = (totals[t.skill2Id] ?: 0) + t.skill2Points
        }

        val def = head.defense + body.defense + arms.defense + waist.defense + legs.defense
        if (def < minDefense) return null

        val armorSlots = head.numSlots + body.numSlots + arms.numSlots + waist.numSlots + legs.numSlots
        val totalSlots = armorSlots + weaponSlots + (talisman?.slots ?: 0)

        var matched = 0
        var matchedPts = 0
        for ((sk, req) in requiredSkills) {
            val act = totals[sk] ?: 0
            if (req > 0 && act < req) return null
            if (req > 0) { matched++; matchedPts += act }
        }

        return ArmorSearchResult(
            headId = head.id, bodyId = body.id, armsId = arms.id,
            waistId = waist.id, legsId = legs.id,
            totalDefense = def, totalSlots = totalSlots,
            skillTotals = totals, matchedSkills = matched,
            totalMatchedPoints = matchedPts
        )
    }

    private fun loadArmorBySlot(hunterType: Int): Map<String, List<ArmorData>> {
        allArmor?.let { cached ->
            return cached.groupBy { it.slot }.mapValues { (_, list) ->
                list.filter { it.hunterType == Armor.ARMOR_TYPE_BOTH || it.hunterType == hunterType }
            }
        }

        val cursor = dataManager.queryArmor()
        val list = mutableListOf<ArmorData>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val a = cursor.armor
            val skills = dataManager.queryItemToSkillTreeArrayItem(a.id)
                .map { it.skillTree.id to it.points }
            list.add(ArmorData(a.id, a.slot ?: "", a.defense, a.numSlots, a.hunterType, skills))
            cursor.moveToNext()
        }
        cursor.close()
        allArmor = list
        return list.groupBy { it.slot }
    }

    private fun armorById(id: Long) = allArmor?.find { it.id == id }

    fun clearCache() { allArmor = null }
}
