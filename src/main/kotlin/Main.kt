import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


fun main() = application {

    var color by remember { mutableStateOf(Color.Red) }
    val state = rememberWindowState(size = DpSize(400.dp, 500.dp))

    Window(onCloseRequest = ::exitApplication, state = state, title = "Basic Color Picker") {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.padding(10.dp).background(Color.White)
        ) {
            ColorPicker { color = it }
            Box(Modifier.size(100.dp).background(color))
        }
    }

}
