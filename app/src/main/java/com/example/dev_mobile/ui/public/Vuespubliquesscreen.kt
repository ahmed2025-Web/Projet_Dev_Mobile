package com.example.dev_mobile.ui.public

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.JeuPublicDto
import com.example.dev_mobile.network.ZonePlanPublicDto

// ── Palette ───────────────────────────────────────────────────────────────────
private val PrimaryDark   = Color(0xFF1A1A2E)
private val PrimaryMid    = Color(0xFF16213E)
private val AccentPurple  = Color(0xFF7C5CBF)
private val AccentBlue    = Color(0xFF4A7FC1)
private val PageBg        = Color(0xFFF4F7FC)
private val CardBg        = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF1A1A2E)
private val TextGray      = Color(0xFF8A8FA3)
private val DividerCol    = Color(0xFFF0F3F8)
private val AvatarBg1     = Color(0xFFE8F0FB)
private val AvatarBg2     = Color(0xFFF3E5F5)
private val AvatarBg3     = Color(0xFFE0F7FA)
private val AvatarBg4     = Color(0xFFFFF8E1)
private val AvatarBg5     = Color(0xFFFFEBEE)

// Couleurs rotationnelles pour les avatars
private val avatarColors = listOf(
    AvatarBg1 to AccentBlue,
    AvatarBg2 to AccentPurple,
    AvatarBg3 to Color(0xFF00838F),
    AvatarBg4 to Color(0xFFF59E0B),
    AvatarBg5 to Color(0xFFD32F2F)
)

private fun avatarColor(index: Int) = avatarColors[index % avatarColors.size]

// ── Écran principal ───────────────────────────────────────────────────────────

@Composable
fun VuesPubliquesScreen(
    festivalId: Int,
    festivalNom: String = "",
    isOnline: Boolean = true,
    vm: VuesPubliquesViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Initialisation dès que le festivalId est connu
    LaunchedEffect(festivalId, festivalNom) {
        vm.init(festivalId, festivalNom)
    }

    // Recharger quand la connexion revient
    LaunchedEffect(isOnline) {
        if (isOnline) vm.loadAll()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(PageBg)) {

        // ── En-tête Hero (style mockup) ───────────────────────────────────────
        HeroHeader(
            festivalNom   = state.festivalNom.ifEmpty { festivalNom },
            dateDebut     = state.festivalDateDebut,
            dateFin       = state.festivalDateFin,
            isOnline      = isOnline
        )

        // ── Barre d'onglets ───────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor   = CardBg,
            contentColor     = AccentPurple,
            divider          = { HorizontalDivider(color = DividerCol) }
        ) {
            listOf("Jeux présentés", "Éditeurs représentés", "Plan du festival")
                .forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick  = {
                            vm.setTab(index)
                            vm.setSearchQuery("") // reset recherche en changeant d'onglet
                        },
                        text = {
                            Text(
                                text       = title,
                                fontSize   = 12.sp,
                                fontWeight = if (state.selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (state.selectedTab == index) AccentPurple else TextGray,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
        }

        // ── Barre de recherche (Jeux + Éditeurs) ─────────────────────────────
        AnimatedVisibility(
            visible = state.selectedTab in listOf(0, 1),
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            SearchBar(
                query    = state.searchQuery,
                onChange = { vm.setSearchQuery(it) },
                hint     = when (state.selectedTab) {
                    0    -> "Rechercher un jeu, éditeur, type…"
                    else -> "Rechercher un éditeur ou un jeu…"
                }
            )
        }

        // ── Contenu selon l'onglet ────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingState()
                !isOnline       -> OfflinePublicState()
                else -> when (state.selectedTab) {
                    0 -> JeuxTab(
                        jeux    = vm.getFilteredJeux(),
                        total   = state.jeux.size,
                        isEmpty = state.jeux.isEmpty()
                    )
                    1 -> EditeurTab(
                        editeurs = vm.getFilteredEditeurs(),
                        total    = state.editeurs.size,
                        isEmpty  = state.editeurs.isEmpty()
                    )
                    2 -> PlanTab(
                        zones   = state.zonesPlan,
                        isEmpty = state.zonesPlan.isEmpty()
                    )
                    else -> {}
                }
            }
        }
    }
}

// ── Hero Header ───────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(
    festivalNom: String,
    dateDebut: String?,
    dateFin: String?,
    isOnline: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryDark, PrimaryMid, AccentPurple.copy(alpha = 0.8f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = festivalNom.ifEmpty { "Festival de jeux" },
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = "Découvrez les jeux et éditeurs présents",
                        fontSize = 13.sp,
                        color    = Color.White.copy(alpha = 0.75f)
                    )
                    // Dates
                    val dateRange = buildString {
                        dateDebut?.take(10)?.let { append(it) }
                        if (dateDebut != null && dateFin != null) append(" → ")
                        dateFin?.take(10)?.let { append(it) }
                    }
                    if (dateRange.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text     = "📅 $dateRange",
                            fontSize = 11.sp,
                            color    = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                // Indicateur offline
                if (!isOnline) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF3E0).copy(alpha = 0.9f)
                    ) {
                        Text(
                            "📡 Hors ligne",
                            fontSize = 10.sp,
                            color    = Color(0xFFE65100),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Barre de recherche ────────────────────────────────────────────────────────

@Composable
private fun SearchBar(query: String, onChange: (String) -> Unit, hint: String) {
    var localQuery by remember(query) { mutableStateOf(query) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = CardBg,
        tonalElevation = 2.dp
    ) {
        OutlinedTextField(
            value         = localQuery,
            onValueChange = {
                localQuery = it
                onChange(it) // On notifie le ViewModel
            },
            placeholder   = { Text(hint, fontSize = 13.sp, color = TextGray) },
            leadingIcon   = { Text("🔍", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp)) },
            trailingIcon  = {
                if (localQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        localQuery = ""
                        onChange("")
                    }) {
                        Text("✕", fontSize = 14.sp, color = TextGray)
                    }
                }
            },
            singleLine    = true,
            // On force un clavier simple pour éviter les bugs de chiffres
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                autoCorrect = false
            ),
            shape         = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor   = AccentPurple.copy(alpha = 0.5f),
                unfocusedContainerColor = PageBg,
                focusedContainerColor   = CardBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
    HorizontalDivider(color = DividerCol)
}

// ── États génériques ──────────────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AccentPurple)
            Spacer(Modifier.height(12.dp))
            Text("Chargement…", fontSize = 13.sp, color = TextGray)
        }
    }
}

@Composable
private fun OfflinePublicState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📡", fontSize = 56.sp)
            Text(
                "Hors ligne",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = TextDark
            )
            Text(
                "Les vues publiques nécessitent une connexion internet.",
                fontSize  = 14.sp,
                color     = TextGray,
                textAlign = TextAlign.Center
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Reconnectez-vous pour voir les jeux et éditeurs présents",
                    fontSize  = 12.sp,
                    color     = Color(0xFFE65100),
                    fontWeight = FontWeight.Medium,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(icon: String, message: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(message, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

// ── TAB 1 : Jeux présentés ────────────────────────────────────────────────────

@Composable
private fun JeuxTab(
    jeux: List<JeuPublicDto>,
    total: Int,
    isEmpty: Boolean
) {
    if (isEmpty) {
        EmptyState(
            icon     = "🎮",
            message  = "Aucun jeu présenté",
            subtitle = "Les jeux placés dans les zones apparaîtront ici"
        )
        return
    }

    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "${jeux.size} jeu(x)${if (jeux.size < total) " trouvé(s) sur $total" else " présenté(s)"}",
                fontSize = 12.sp,
                color    = TextGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(jeux) { jeu ->
            JeuCard(jeu = jeu, index = jeux.indexOf(jeu))
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun JeuCard(jeu: JeuPublicDto, index: Int) {
    val (avatarBg, avatarFg) = avatarColor(index)

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {

            Box(
                modifier            = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment    = Alignment.Center
            ) {
                Text(
                    text       = jeu.jeu_nom.take(1).uppercase(),
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = avatarFg
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Nom + badge zone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = jeu.jeu_nom,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextDark,
                        modifier   = Modifier.weight(1f, fill = false),
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    jeu.zone_plan_nom?.let { zone ->
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentPurple.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text     = zone,
                                fontSize = 9.sp,
                                color    = AccentPurple,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Éditeur
                Text(
                    text     = jeu.editeur_nom,
                    fontSize = 12.sp,
                    color    = AccentBlue,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = DividerCol)
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    jeu.type_jeu?.let {
                        DetailChip(label = "Type", value = it)
                    }
                    val ageLabel = buildString {
                        jeu.age_mini?.let { append("${it} ans") }
                    }
                    if (ageLabel.isNotBlank()) {
                        DetailChip(label = "Âge", value = ageLabel)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val joueursLabel = buildString {
                        jeu.joueurs_mini?.let { min ->
                            jeu.joueurs_maxi?.let { max ->
                                if (min == max) append("$min joueur(s)")
                                else append("$min-$max joueurs")
                            } ?: append("$min+ joueurs")
                        }
                    }
                    if (joueursLabel.isNotBlank()) {
                        DetailChip(label = "Joueurs", value = joueursLabel)
                    }
                    jeu.duree_moyenne?.let {
                        DetailChip(label = "Durée", value = "${it} min")
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text     = "✅ Exemplaires disponibles : ${jeu.nombre_exemplaires}",
                            fontSize = 11.sp,
                            color    = Color(0xFF388E3C),
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Présenté par (si différent de l'éditeur)
                jeu.presente_par?.let { par ->
                    if (par != jeu.editeur_nom) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Présenté par : $par",
                            fontSize = 10.sp,
                            color    = TextGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text     = "$label :",
            fontSize = 11.sp,
            color    = TextGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text     = value,
            fontSize = 11.sp,
            color    = TextDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── TAB 2 : Éditeurs représentés ──────────────────────────────────────────────

@Composable
private fun EditeurTab(
    editeurs: List<EditeurAvecJeux>,
    total: Int,
    isEmpty: Boolean
) {
    if (isEmpty) {
        EmptyState(
            icon     = "🏢",
            message  = "Aucun éditeur représenté",
            subtitle = "Les éditeurs dont les jeux sont exposés apparaîtront ici"
        )
        return
    }

    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "${editeurs.size} éditeur(s)${if (editeurs.size < total) " trouvé(s) sur $total" else " représenté(s)"}",
                fontSize = 12.sp,
                color    = TextGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(editeurs) { editeur ->
            EditeurCard(editeur = editeur, index = editeurs.indexOf(editeur))
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun EditeurCard(editeur: EditeurAvecJeux, index: Int) {
    val (avatarBg, avatarFg) = avatarColor(index)
    var expanded by remember { mutableStateOf(true) }

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // En-tête éditeur
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.clickable { expanded = !expanded }
            ) {
                // Avatar
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {

                    Text("🏢", fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text       = editeur.editeurNom,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark
                    )
                    Text(
                        text     = "${editeur.nbJeuxPresentes} jeu(x) présenté(s)",
                        fontSize = 12.sp,
                        color    = TextGray
                    )
                }

                // Chevron
                Text(
                    text     = if (expanded) "▲" else "▼",
                    fontSize = 12.sp,
                    color    = TextGray
                )
            }

            // Liste des jeux (expansible)
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = DividerCol)
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text     = "Jeux :",
                        fontSize = 12.sp,
                        color    = TextGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))

                    if (editeur.jeux.isEmpty()) {
                        Text(
                            "• Aucun jeu listé",
                            fontSize = 13.sp,
                            color    = TextGray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        editeur.jeux.forEach { jeu ->
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(6.dp),
                                    shape    = CircleShape,
                                    color    = avatarFg.copy(alpha = 0.7f),
                                    content  = {}
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text     = jeu.jeu_nom,
                                        fontSize = 13.sp,
                                        color    = TextDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val subtitle = buildString {
                                        jeu.type_jeu?.let { append(it) }
                                        jeu.zone_plan_nom?.let {
                                            if (isNotEmpty()) append(" · ")
                                            append(it)
                                        }
                                    }
                                    if (subtitle.isNotBlank()) {
                                        Text(subtitle, fontSize = 11.sp, color = TextGray)
                                    }
                                }
                                // Nb exemplaires
                                if (jeu.nombre_exemplaires > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AccentBlue.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            "×${jeu.nombre_exemplaires}",
                                            fontSize   = 10.sp,
                                            color      = AccentBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── TAB 3 : Plan du festival ──────────────────────────────────────────────────

@Composable
private fun PlanTab(
    zones: List<ZonePlanPublicDto>,
    isEmpty: Boolean
) {
    if (isEmpty) {
        EmptyState(
            icon     = "🗺️",
            message  = "Plan non disponible",
            subtitle = "Les zones du festival apparaîtront ici une fois configurées"
        )
        return
    }

    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "${zones.size} zone(s) disponible(s)",
                fontSize = 12.sp,
                color    = TextGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(zones) { zone ->
            ZoneCard(zone = zone)
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ZoneCard(zone: ZonePlanPublicDto) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône localisation (style mockup)
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FB)),
                contentAlignment = Alignment.Center
            ) {
                Text("📍", fontSize = 22.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text       = zone.nom,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text     = "Capacité : ${zone.nombre_tables_total} table(s)",
                    fontSize = 12.sp,
                    color    = TextGray
                )

                val nbJeux = zone.nb_jeux_places
                if (nbJeux > 0) {
                    Text(
                        text     = "$nbJeux espace(s) de jeu",
                        fontSize = 12.sp,
                        color    = TextGray
                    )
                }
            }

            // Badge tables disponibles
            val dispo = zone.tables_disponibles
            if (dispo != null) {
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (dispo > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Column(
                        modifier            = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text       = "$dispo",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (dispo > 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
                        )
                        Text(
                            text     = "libre(s)",
                            fontSize = 9.sp,
                            color    = TextGray
                        )
                    }
                }
            }
        }
    }
}