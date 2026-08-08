package com.simplelauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.simplelauncher.databinding.FragmentApplistBinding
import kotlinx.coroutines.*

class AppListFragment : Fragment() {

    private var _binding: FragmentApplistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AppListAdapter
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppListAdapter { app ->
            launchApp(app)
        }

        binding.appList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.appList.adapter = adapter

        scope.launch {
            allApps = withContext(Dispatchers.IO) { getInstalledApps() }
            adapter.submitList(allApps)
        }

        binding.searchInput.requestFocus()
        showKeyboard()

        binding.searchInput.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            val filtered = if (query.isEmpty()) {
                allApps
            } else {
                allApps.filter {
                    it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
                }
            }
            adapter.submitList(filtered)

            if (query.isNotEmpty() && filtered.size == 1) {
                scope.launch {
                    delay(300)
                    launchApp(filtered[0])
                }
            } else if (query.isNotEmpty()) {
                val perfect = filtered.find { it.label.equals(query, ignoreCase = true) }
                if (perfect != null) {
                    scope.launch {
                        delay(300)
                        launchApp(perfect)
                    }
                }
            }
        }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else false
        }
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.searchInput.postDelayed({
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }

    private fun launchApp(app: AppInfo) {
        val intent = requireContext().packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            (activity as? MainActivity)?.closeAppList()
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return requireContext().packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != requireContext().packageName }
            .map {
                AppInfo(
                    label = it.loadLabel(requireContext().packageManager).toString(),
                    packageName = it.activityInfo.packageName
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        scope.cancel()
    }
}
