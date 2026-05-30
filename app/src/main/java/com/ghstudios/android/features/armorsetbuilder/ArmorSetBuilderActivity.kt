package com.ghstudios.android.features.armorsetbuilder

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ghstudios.android.GenericActivity
import com.ghstudios.android.MenuSection
import com.ghstudios.android.mhgendatabase.R

/**
 * Activity hosting the Armor Set Builder search screen.
 */
class ArmorSetBuilderActivity : GenericActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_armor_set_builder)
        super.setAsTopLevel()
    }

    override fun getSelectedSection(): Int {
        return MenuSection.ARMOR_SET_BUILDER
    }

    override fun createFragment(): Fragment {
        return ArmorSetBuilderFragment()
    }
}
