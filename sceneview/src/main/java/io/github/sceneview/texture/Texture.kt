package io.github.sceneview.texture

import androidx.lifecycle.Lifecycle
import com.google.android.filament.Stream
import com.google.android.filament.Texture
import com.gorisse.thomas.lifecycle.observe
import io.github.sceneview.Filament

fun <R> Texture.use(block: (Texture) -> R): R = block(this).also { destroy() }
fun Texture.Builder.build(lifecycle: Lifecycle): Texture = build(Filament.engine)
    .also { texture ->
        lifecycle.observe(onDestroy = {
            // Prevent double destroy in case of manually destroyed
            runCatching { texture.destroy() }
        })
    }

fun Texture.setExternalStream(stream: Stream) = setExternalStream(Filament.engine, stream)
fun Texture.setImage(level: Int, buffer: Texture.PixelBufferDescriptor) =
    setImage(Filament.engine, level, buffer)

/**
 * Destroys a Texture and frees all its associated resources.
 */
fun Texture.destroy() = Filament.engine.destroyTexture(this)