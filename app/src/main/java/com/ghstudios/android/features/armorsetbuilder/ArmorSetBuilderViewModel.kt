package com.ghstudios.android.features.armorsetbuilder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ghstudios.android.data.DataManager
import com.ghstudios.android.data.classes.SkillTree
import com.ghstudios.android.data.util.SearchFilter
import com.ghstudios.android.util.toList

/**
 * Holds a SkillTree along with its available skill levels (name + required points).
 */
data class SkillTreeWithLevels(
    val skillTree: SkillTree,
    val skillNames: List<String>,   // e.g. ["Attack Up (S)", "Attack Up (M)", "Attack Up (L)"]
    val requiredPoints: List<Int>   // e.g. [10, 15, 20]
)

/**
 * Tracks a user's skill filter selection: which skill tree and at what required points.
 */
data class SkillFilterSelection(
    val skillTreeId: Long,
    val requiredPoints: Int
)

/**
 * ViewModel for the Armor Set Builder search screen.
 */
class ArmorSetBuilderViewModel(app: Application) : AndroidViewModel(app) {

    private val dataManager = DataManager.get()

    /** All skill trees with their levels (names + points), loaded once */
    private val allSkillTrees: List<SkillTreeWithLevels> = run {
        dataManager.querySkillTrees().toList { it.skillTree }.map { tree ->
            val skills = dataManager.querySkillsFromTree(tree.id).toList { it.skill }
            val names = skills.map { it.name ?: "" }
            val points = skills.map { it.requiredPoints }
            SkillTreeWithLevels(tree, names, points)
        }
    }

    /** Current filter text for skill search (matches tree name + skill names) */
    private val skillFilter = MutableLiveData<String>()

    /** Filtered list of SkillTreeWithLevels matching the current filter */
    val filteredSkills: LiveData<List<SkillTreeWithLevels>> = Transformations.map(skillFilter) { query ->
        val filter = SearchFilter(query ?: "")
        allSkillTrees.filter { item ->
            filter.matches(item.skillTree.name) || item.skillNames.any { filter.matches(it) }
        }
    }

    /** Map of skillTreeId -> selected required points (null means not selected) */
    private val _selectedSkills = MutableLiveData<Map<Long, Int>>(emptyMap())
    val selectedSkills: LiveData<Map<Long, Int>> = _selectedSkills

    /** Count of selected skills */
    val selectedCount: LiveData<Int> = Transformations.map(_selectedSkills) { it.size }

    init {
        skillFilter.value = ""
    }

    fun setFilter(query: String) {
        skillFilter.value = query
    }

    /** Returns the currently selected required points for a skill tree, or null if not selected */
    fun getSelectedPoints(skillTreeId: Long): Int? {
        return _selectedSkills.value?.get(skillTreeId)
    }

    fun isSkillSelected(skillTreeId: Long): Boolean {
        return _selectedSkills.value?.containsKey(skillTreeId) == true
    }

    /** Toggle selection on/off. When turning on, defaults to the highest available point threshold. */
    fun toggleSkill(skillTreeId: Long, defaultPoints: Int) {
        val current = _selectedSkills.value?.toMutableMap() ?: mutableMapOf()
        if (current.containsKey(skillTreeId)) {
            current.remove(skillTreeId)
        } else {
            current[skillTreeId] = defaultPoints
        }
        _selectedSkills.value = current
    }

    /** Update the required points for an already-selected skill tree */
    fun setSkillPoints(skillTreeId: Long, points: Int) {
        val current = _selectedSkills.value?.toMutableMap() ?: mutableMapOf()
        if (current.containsKey(skillTreeId)) {
            current[skillTreeId] = points
            _selectedSkills.value = current
        }
    }

    fun buildSearchParams(): ArmorSetSearchParams {
        return ArmorSetSearchParams(
            selectedSkills = _selectedSkills.value?.map { (treeId, points) ->
                SkillFilterSelection(treeId, points)
            } ?: emptyList()
        )
    }
}

data class ArmorSetSearchParams(
    val gender: Int = 2,
    val hunterType: Int = 0,
    val rarity: Int = 0,
    val minSlots: Int = 0,
    val defenseMin: Int = 0,
    val defenseMax: Int = 0,
    val selectedSkills: List<SkillFilterSelection> = emptyList()
)
