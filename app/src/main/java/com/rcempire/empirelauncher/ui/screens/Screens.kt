package com.rcempire.empirelauncher.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcempire.empirelauncher.R
import com.rcempire.empirelauncher.accounts.Account
import com.rcempire.empirelauncher.accounts.OfflineAccounts
import com.rcempire.empirelauncher.crash.CrashAnalyzer
import com.rcempire.empirelauncher.data.model.ModrinthHit
import com.rcempire.empirelauncher.data.network.ModrinthApi
import com.rcempire.empirelauncher.data.network.MojangApi
import com.rcempire.empirelauncher.performance.RendererAdvisor
import com.rcempire.empirelauncher.performance.SmartOptimizer
import com.rcempire.empirelauncher.ui.theme.EmpireGradient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable private fun Page(title: String, content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        content()
    }

@Composable private fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) =
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(EmpireGradient).padding(horizontal = 4.dp)) {
        TextButton(onClick, Modifier.fillMaxWidth()) { Text(text, color = androidx.compose.ui.graphics.Color.White) }
    }

// ---------------- HOME ----------------
@Composable
fun HomeScreen(onControls: () -> Unit, onLogs: () -> Unit, vm: HomeViewModel = hiltViewModel()) {
    val latest by vm.latest.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Image(painterResource(R.drawable.rc_logo), "Empire Launcher", Modifier.size(200.dp))
        Text("Latest Minecraft release: $latest", style = MaterialTheme.typography.bodyLarge)
        GradientButton("PLAY", { /* TODO: launch last instance via runtime */ }, Modifier.fillMaxWidth().height(56.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onControls) { Text("Controls") }
            OutlinedButton(onLogs) { Text("Crash helper") }
        }
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val mojang: MojangApi) : ViewModel() {
    private val _latest = MutableStateFlow("loading...")
    val latest: StateFlow<String> = _latest
    init { viewModelScope.launch { _latest.value = runCatching { mojang.versionManifest().latest.release }.getOrDefault("offline") } }
}

// ---------------- INSTANCES ----------------
@Composable
fun InstancesScreen() = Page("Instances") {
    val optimize = remember { SmartOptimizer.optimize(totalRamMb = 6144, gpu = "Adreno 650", mcVersion = "1.21.1") }
    Card { Column(Modifier.padding(16.dp)) {
        Text("Smart Optimize preview (demo device: 6 GB, Adreno)")
        Text("RAM ${optimize.ramMb} MB • render distance ${optimize.renderDistance} • ${optimize.renderer}")
    } }
    Text("TODO: Room-backed instance list, create/edit instance, version + loader picker.", style = MaterialTheme.typography.bodySmall)
}

// ---------------- STORE ----------------
@HiltViewModel
class StoreViewModel @Inject constructor(private val api: ModrinthApi) : ViewModel() {
    val results = MutableStateFlow<List<ModrinthHit>>(emptyList())
    val error = MutableStateFlow<String?>(null)
    fun search(q: String, type: String = "mod", mc: String = "1.21.1", loader: String = "fabric") = viewModelScope.launch {
        runCatching { api.search(q, "[[\"project_type:$type\"],[\"versions:$mc\"],[\"categories:$loader\"]]").hits }
            .onSuccess { results.value = it; error.value = null }
            .onFailure { error.value = it.message }
    }
}

@Composable
fun StoreScreen(vm: StoreViewModel = hiltViewModel()) {
    var q by remember { mutableStateOf("") }
    val hits by vm.results.collectAsState()
    val err by vm.error.collectAsState()
    LaunchedEffect(Unit) { vm.search("sodium") }
    Page("Mod Store (Modrinth)") {
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Search mods") }, singleLine = true,
            trailingIcon = { TextButton({ vm.search(q) }) { Text("Go") } })
        err?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(hits) { h ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    Text(h.title, style = MaterialTheme.typography.titleMedium)
                    Text(h.description, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                    Text("${h.downloads} downloads", style = MaterialTheme.typography.labelSmall)
                } }
            }
        }
    }
}

// ---------------- ACCOUNTS ----------------
@Composable
fun AccountsScreen() {
    var name by remember { mutableStateOf("") }
    var accounts by remember { mutableStateOf(listOf<Account>()) }
    var err by remember { mutableStateOf<String?>(null) }
    Page("Accounts") {
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Offline username") }, singleLine = true)
        GradientButton("Add offline account", {
            runCatching { OfflineAccounts.create(name) }
                .onSuccess { accounts = accounts + it; name = ""; err = null }
                .onFailure { err = it.message }
        }, Modifier.fillMaxWidth())
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        OutlinedButton({ /* TODO: MicrosoftAuth.requestDeviceCode() -> show code -> pollAndLogin() */ }, Modifier.fillMaxWidth()) {
            Text("Sign in with Microsoft")
        }
        accounts.forEach { Text("${it.username}  (${it.type})") }
    }
}

// ---------------- CONTROLS ----------------
@Composable
fun ControlsScreen() = Page("Controls") {
    Text("TODO: drag-and-drop layout editor (tap / hold / toggle / joystick / swipe / macro), gamepad, gyro.")
}

// ---------------- LOGS ----------------
@Composable
fun LogsScreen() {
    var log by remember { mutableStateOf("") }
    var hints by remember { mutableStateOf(listOf<com.rcempire.empirelauncher.crash.CrashHint>()) }
    Page("Crash helper") {
        OutlinedTextField(log, { log = it }, Modifier.fillMaxWidth().height(160.dp), label = { Text("Paste crash log") })
        GradientButton("Analyze", { hints = CrashAnalyzer.analyze(log) }, Modifier.fillMaxWidth())
        hints.forEach { Card { Column(Modifier.padding(12.dp)) {
            Text(it.title, style = MaterialTheme.typography.titleMedium); Text(it.fix) } } }
    }
}

// ---------------- SETTINGS / ABOUT ----------------
@Composable
fun SettingsScreen(onLogs: () -> Unit, onControls: () -> Unit) = Page("Settings") {
    Text("Renderers: ${RendererAdvisor.all.joinToString()}", style = MaterialTheme.typography.bodySmall)
    OutlinedButton(onControls, Modifier.fillMaxWidth()) { Text("Controls") }
    OutlinedButton(onLogs, Modifier.fillMaxWidth()) { Text("Crash helper") }
    HorizontalDivider()
    Text("About", style = MaterialTheme.typography.titleMedium)
    Text("Empire Launcher by RC Empire", style = MaterialTheme.typography.bodyMedium)
    Text("Empire Launcher is not affiliated with Mojang or Microsoft. Minecraft is a trademark of Mojang AB. " +
        "You must own the game and accept the Minecraft EULA. No Minecraft assets are bundled.",
        style = MaterialTheme.typography.bodySmall)
    Text("Based on / credits: ZalithLauncher, PojavLauncher (GPL-3.0), Modrinth API, Retrofit, OkHttp, " +
        "kotlinx.serialization, Hilt, Jetpack Compose. See CREDITS.md.", style = MaterialTheme.typography.bodySmall)
}
