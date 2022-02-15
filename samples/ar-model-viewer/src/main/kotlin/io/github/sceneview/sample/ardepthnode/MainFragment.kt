package io.github.sceneview.sample.ardepthnode

import android.os.Bundle
import android.view.*
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.utils.doOnApplyWindowInsets

class MainFragment : Fragment(R.layout.fragment_main) {

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton

    lateinit var modelNode: ArModelNode

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        sceneView = view.findViewById(R.id.sceneView)
        loadingView = view.findViewById(R.id.loadingView)
        actionButton = view.findViewById<ExtendedFloatingActionButton>(R.id.actionButton).apply {
            // Add system bar margins
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener(::actionButtonClicked)
        }

        isLoading = true
        modelNode = ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
            loadModel(
                context = requireContext(),
                coroutineScope = lifecycleScope,
                glbFileLocation = "models/spiderbot.glb",
                autoAnimate = true,
                onLoaded = { isLoading = false })
            onTrackingChanged = { _, isTracking, _ ->
                actionButton.isGone = !isTracking
            }
        }
        sceneView.addChild(modelNode)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked
        modelNode.detachAnchor()
        modelNode.placementMode = when (item.itemId) {
            R.id.menuPlanePlacement -> PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL
            R.id.menuInstantPlacement -> PlacementMode.INSTANT
            R.id.menuDepthPlacement -> PlacementMode.DEPTH
            R.id.menuBestPlacement -> PlacementMode.BEST_AVAILABLE
            else -> PlacementMode.DISABLED
        }
        return true
    }

    fun actionButtonClicked(view: View? = null) {
        if (!modelNode.isAnchored && modelNode.anchor()) {
            actionButton.text = getString(R.string.move_object)
            actionButton.icon = resources.getDrawable(R.drawable.ic_target)
        } else {
            modelNode.anchor = null
            actionButton.text = getString(R.string.place_object)
            actionButton.icon = resources.getDrawable(R.drawable.ic_anchor)
        }
    }
}