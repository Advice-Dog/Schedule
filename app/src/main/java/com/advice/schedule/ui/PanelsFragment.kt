package com.advice.schedule.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.advice.schedule.get
import com.advice.schedule.ui.activities.MainActivity
import com.advice.schedule.ui.schedule.ScheduleFragment
import com.advice.schedule.utilities.Analytics
import com.discord.panels.OverlappingPanelsLayout
import com.discord.panels.PanelState
import com.shortstack.hackertracker.R
import com.shortstack.hackertracker.databinding.PanelsFragmentBinding
import org.koin.core.KoinComponent
import org.koin.core.inject

class PanelsFragment : Fragment(), KoinComponent {

    private val analytics by inject<Analytics>()

    private var _binding: PanelsFragmentBinding? = null
    private val binding get() = _binding!!

    private var isOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PanelsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // force hide the bottom navigation bar
        binding.bottomNavigation.translationY = 300f

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    binding.overlappingPanels.closePanels()
                    false
                }
                R.id.nav_information -> {
                    showInformation()
                    false
                }
                R.id.nav_map -> {
                    showMap()
                    false
                }

                R.id.nav_settings -> {
                    showSettings()
                    false
                }
                else -> false
            }
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (binding.overlappingPanels.getSelectedPanel()) {
                    OverlappingPanelsLayout.Panel.START -> {
                        requireActivity().onBackPressed()
                    }
                    OverlappingPanelsLayout.Panel.CENTER -> {
                        binding.overlappingPanels.openStartPanel()
                    }
                    OverlappingPanelsLayout.Panel.END -> {
                        binding.overlappingPanels.closePanels()
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)

        binding.overlappingPanels.registerStartPanelStateListeners(object : OverlappingPanelsLayout.PanelStateListener {

            private var previousScreen: String? = null

            override fun onPanelStateChange(panelState: PanelState) {
                when (panelState) {
                    PanelState.Opening,
                    PanelState.Opened -> showBottomNavigation()
                    PanelState.Closing,
                    PanelState.Closed -> hideBottomNavigation()
                }

                onBackPressedCallback.isEnabled = when (binding.overlappingPanels.getSelectedPanel()) {
                    OverlappingPanelsLayout.Panel.START -> false
                    OverlappingPanelsLayout.Panel.CENTER,
                    OverlappingPanelsLayout.Panel.END -> true
                }

                val screen = when (binding.overlappingPanels.getSelectedPanel()) {
                    OverlappingPanelsLayout.Panel.START -> Analytics.SCREEN_HOME
                    OverlappingPanelsLayout.Panel.CENTER -> Analytics.SCREEN_SCHEDULE
                    OverlappingPanelsLayout.Panel.END -> Analytics.SCREEN_FILTERS
                }
                if (screen != previousScreen) {
                    previousScreen = screen
                    analytics.setScreen(screen)
                }
            }
        })

        // to ensure the default screen is tracked
        analytics.setScreen(Analytics.SCREEN_SCHEDULE)
    }

    private fun showInformation() {
        (context as MainActivity).showInformation()
    }

    private fun showMap() {
        (context as MainActivity).showMap()
    }

    private fun showSettings() {
        (context as MainActivity).showSettings()
    }

    private fun hideBottomNavigation() {
        if (!isOpen) {
            return
        }
        isOpen = false
        binding.bottomNavigation.clearAnimation()
        ObjectAnimator.ofFloat(
            binding.bottomNavigation,
            "translationY",
            binding.bottomNavigation.height.toFloat()
        ).apply {
            duration = ANIMATION_DURATION
            start()
        }
    }

    private fun showBottomNavigation() {
        if (isOpen) {
            return
        }
        isOpen = true
        binding.bottomNavigation.clearAnimation()
        ObjectAnimator.ofFloat(binding.bottomNavigation, "translationY", 0f).apply {
            duration = ANIMATION_DURATION
            start()
        }
    }

    fun openStartPanel() {
        binding.overlappingPanels.openStartPanel()
    }

    fun openEndPanel() {
        binding.overlappingPanels.openEndPanel()
    }

    fun invalidate() {
        val schedule = childFragmentManager.fragments.get(ScheduleFragment::class.java)
        schedule.invalidate()
    }

    companion object {
        private const val ANIMATION_DURATION = 250L
    }
}