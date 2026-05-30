package com.ghstudios.android.features.armorsetbuilder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghstudios.android.mhgendatabase.R

/**
 * Fragment for the Armor Set Builder search screen.
 * Allows users to filter armor by gender, type, rarity, weapon slots, defense range,
 * and select desired skills before searching.
 */
class ArmorSetBuilderFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(ArmorSetBuilderViewModel::class.java)
    }

    private lateinit var skillAdapter: SkillFilterAdapter

    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerRarity: Spinner
    private lateinit var spinnerSlots: Spinner
    private lateinit var editDefenseMin: EditText
    private lateinit var editDefenseMax: EditText
    private lateinit var editSkillFilter: EditText
    private lateinit var recyclerSkills: RecyclerView
    private lateinit var textSelectedCount: TextView
    private lateinit var buttonSearch: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_armor_set_builder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        spinnerGender = view.findViewById(R.id.spinner_gender)
        spinnerType = view.findViewById(R.id.spinner_type)
        spinnerRarity = view.findViewById(R.id.spinner_rarity)
        spinnerSlots = view.findViewById(R.id.spinner_slots)
        editDefenseMin = view.findViewById(R.id.edit_defense_min)
        editDefenseMax = view.findViewById(R.id.edit_defense_max)
        editSkillFilter = view.findViewById(R.id.edit_skill_filter)
        recyclerSkills = view.findViewById(R.id.recycler_skills)
        textSelectedCount = view.findViewById(R.id.text_selected_count)
        buttonSearch = view.findViewById(R.id.button_search)

        setupSkillList()
        setupFilterInput()
        setupSearchButton()
        observeViewModel()
    }

    private fun setupSkillList() {
        skillAdapter = SkillFilterAdapter(viewModel)
        recyclerSkills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = skillAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupFilterInput() {
        editSkillFilter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setFilter(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Map spinner index to actual armor gender value.
     * Spinner: 0=不限, 1=男, 2=女  →  Armor.gender: 2=Any, 1=Male, 0=Female
     */
    private fun getGenderValue(spinnerIndex: Int): Int = when (spinnerIndex) {
        0 -> 2  // 不限
        1 -> 1  // 男
        2 -> 0  // 女
        else -> 2
    }

    private fun setupSearchButton() {
        buttonSearch.setOnClickListener {
            val params = viewModel.buildSearchParams().copy(
                gender = getGenderValue(spinnerGender.selectedItemPosition),
                hunterType = spinnerType.selectedItemPosition,
                rarity = spinnerRarity.selectedItemPosition,
                minSlots = spinnerSlots.selectedItemPosition,
                defenseMin = editDefenseMin.text.toString().toIntOrNull() ?: 0,
                defenseMax = editDefenseMax.text.toString().toIntOrNull() ?: 0
            )

            val count = params.selectedSkills.size
            val msg = "Search params: gender=${params.gender}, type=${params.hunterType}, " +
                    "rarity=${params.rarity}, slots>=${params.minSlots}, " +
                    "defense ${params.defenseMin}-${params.defenseMax}, skills=$count"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        viewModel.filteredSkills.observe(viewLifecycleOwner, Observer { skills ->
            skillAdapter.submitList(skills)
        })

        viewModel.selectedCount.observe(viewLifecycleOwner, Observer { count ->
            textSelectedCount.text = getString(R.string.asb_action_select) + " $count"
        })
    }
}

/**
 * RecyclerView adapter for the skill filter list.
 * Each item displays a SkillTree name with a checkbox and a points dropdown.
 */
class SkillFilterAdapter(
    private val viewModel: ArmorSetBuilderViewModel
) : RecyclerView.Adapter<SkillFilterAdapter.ViewHolder>() {

    private var skills: List<SkillTreeWithLevels> = emptyList()

    fun submitList(list: List<SkillTreeWithLevels>) {
        skills = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skill_filter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(skills[position])
    }

    override fun getItemCount(): Int = skills.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: android.widget.CheckBox = itemView.findViewById(R.id.checkbox_skill)
        private val nameView: TextView = itemView.findViewById(R.id.text_skill_name)
        private val spinnerPoints: Spinner = itemView.findViewById(R.id.spinner_points)

        fun bind(item: SkillTreeWithLevels) {
            val tree = item.skillTree

            nameView.text = tree.name

            // Set up spinner with skill names + point thresholds
            val spinnerLabels = item.skillNames.zip(item.requiredPoints).map { (name, pts) ->
                "$name ($pts 点)"
            }
            val pointsAdapter = android.widget.ArrayAdapter(
                itemView.context,
                android.R.layout.simple_spinner_item,
                spinnerLabels
            )
            pointsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPoints.adapter = pointsAdapter

            val isSelected = viewModel.isSkillSelected(tree.id)

            // Update checkbox without triggering listener
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = isSelected

            // Enable/disable spinner based on selection
            val hasPoints = item.requiredPoints.isNotEmpty()
            spinnerPoints.isEnabled = isSelected && hasPoints
            spinnerPoints.alpha = if (isSelected && hasPoints) 1.0f else 0.4f

            // Restore previously selected point value
            val savedPoints = viewModel.getSelectedPoints(tree.id)
            if (savedPoints != null) {
                val idx = item.requiredPoints.indexOf(savedPoints)
                if (idx >= 0) spinnerPoints.setSelection(idx)
            }

            // Toggle selection on checkbox change
            checkbox.setOnCheckedChangeListener { _, checked ->
                val defaultPoints = item.requiredPoints.firstOrNull() ?: 10
                viewModel.toggleSkill(tree.id, defaultPoints)
                notifyItemChanged(adapterPosition)
            }

            // Update points on spinner selection
            spinnerPoints.onItemSelectedListener = null
            spinnerPoints.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val points = item.requiredPoints.getOrNull(pos) ?: return
                    viewModel.setSkillPoints(tree.id, points)
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

            // Click on the whole row toggles the checkbox
            itemView.setOnClickListener {
                checkbox.toggle()
            }
        }
    }
}
