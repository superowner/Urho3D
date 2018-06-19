package com.github.urho3d.launcher

import android.app.ExpandableListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import com.github.urho3d.UrhoActivity

class LauncherActivity : ExpandableListActivity() {

    // Filter to only include filename that has an extension
    private fun getScriptNames(path: String) = assets.list(path).filter { it.contains('.') }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only the sample library is selectable, excluding Urho3DPlayer which is handled separately
        val regex = Regex("^(?:Urho3D.*|.+_shared)\$")
        val libraryNames = UrhoActivity.getLibraryNames(this)
        val items = mutableMapOf("C++" to libraryNames.filterNot { regex.matches(it) })
        if (libraryNames.find { it == "Urho3DPlayer" } != null) {
            items.putAll(mapOf(
                    // FIXME: Should not assume both scripting subsystems are enabled in the build
                    "AngelScript" to getScriptNames("Data/Scripts"),
                    "Lua" to getScriptNames("Data/LuaScripts")
            ))
        }
        items.filterValues { it.isEmpty() }.forEach { items.remove(it.key) }

        setListAdapter(SimpleExpandableListAdapter(this,
                items.map {
                    mapOf("api" to it.key, "info" to "Click to expand/collapse")
                },
                android.R.layout.simple_expandable_list_item_2,
                arrayOf("api", "info"),
                intArrayOf(android.R.id.text1, android.R.id.text2),
                items.map {
                    it.value.map {
                        mapOf("item" to it)
                    }
                },
                R.layout.launcher_list_item,
                arrayOf("item"),
                intArrayOf(android.R.id.text1)
        ))
        setContentView(R.layout.activity_launcher)
    }

    override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int,
                              childPosition: Int, id: Long): Boolean {
        @Suppress("UNCHECKED_CAST")
        val item = (expandableListAdapter.getChild(groupPosition, childPosition) as Map<String, String>)["item"]
        if (item != null) {
            // Start main activity with the intention to load the selected library name
            startActivity(Intent(this, MainActivity::class.java)
                    .putExtra(MainActivity.argument,
                            if (item.contains('.')) {
                                if (item.endsWith(".as")) "Urho3DPlayer:Scripts/$item"
                                else "Urho3DPlayer:LuaScripts/$item"
                            } else item
                    )
            )
        }
        finish()
        return true
    }

}
