package com.smarttoolfactory.tutorial1_1basics

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.smarttoolfactory.tutorial1_1basics.model.SuggestionModel
import com.smarttoolfactory.tutorial1_1basics.model.TutorialSectionModel
import com.smarttoolfactory.tutorial1_1basics.ui.components.CancelableChip
import com.smarttoolfactory.tutorial1_1basics.ui.components.JumpToBottom
import com.smarttoolfactory.tutorial1_1basics.ui.components.StaggeredGrid
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialSectionCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val tabList = listOf("Components", "Layout", "State", "Gesture", "Graphics", "Theming")

/**
 * This is Home Screen that contains Search bar, Tabs, and tutorial pages in Pager
 */
@OptIn(ExperimentalPagerApi::class)
@ExperimentalAnimationApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    navigateToTutorial: (String) -> Unit,
    state: SearchState<TutorialSectionModel, SuggestionModel> = rememberSearchState()
) {
//    println("✅ HomeScreen() state:\n$state")

    state.suggestions = viewModel.suggestionState.collectAsState(initial = suggestionList).value

    Column(
        modifier = modifier.fillMaxSize()
    ) {


        // FIXME Crashes when back button is pressed before any search
/*
         val dispatcher: OnBackPressedDispatcher =
            LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
        val context = LocalContext.current

        val backCallback = remember {
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (state.query.text.isEmpty()) {
                        dispatcher.onBackPressed()
                    } else {
//                        Toast.makeText(context, "Back", Toast.LENGTH_SHORT).show()
                        state.query = TextFieldValue("")
                    }

                }
            }
        }

        DisposableEffect(dispatcher) { // dispose/relaunch if dispatcher changes
            dispatcher.addCallback(backCallback)
            onDispose {
//                Toast.makeText(context, "Disposed", Toast.LENGTH_SHORT).show()

                backCallback.remove() // avoid leaks!
            }
        }
*/

        SearchBar(
            query = state.query,
            onQueryChange = { state.query = it },
            onSearchFocusChange = { state.focused = it },
            onClearQuery = { state.query = TextFieldValue("") },
            onBack = { state.query = TextFieldValue("") },
            searching = state.searching,
            focused = state.focused,
            modifier = modifier
        )

        LaunchedEffect(state.query.text) {
            state.searching = true
//            println("⚠️ HomeScreen() LaunchedEffect query: ${state.query.text}, searching: ${state.searching}")
            delay(100)
            state.searchResults = viewModel.getTutorials(state.query.text)
            state.searching = false
        }

        when (state.searchDisplay) {
            SearchDisplay.InitialResults -> {
                HomeContent(modifier, viewModel.tutorialList, navigateToTutorial)
            }
            SearchDisplay.NoResults -> {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❌ No Results!", fontSize = 24.sp, color = Color(0xffDD2C00))
                }
            }

            SearchDisplay.Suggestions -> {
                SuggestionGridLayout(suggestions = state.suggestions) {
                    var text = state.query.text
                    if (text.isEmpty()) text = it else text += " $it"
                    text.trim()
                    // Set text and cursor position to end of text
                    state.query = TextFieldValue(text, TextRange(text.length))
                }
            }

            SearchDisplay.Results -> {
                TutorialListContent(modifier, state.searchResults, navigateToTutorial)
            }
        }
    }
}

@Composable
private fun SuggestionGridLayout(
    modifier: Modifier = Modifier,
    suggestions: List<SuggestionModel>,
    onSuggestionClick: (String) -> Unit
) {


    StaggeredGrid(
        modifier = modifier.padding(4.dp)
    ) {
        suggestions.forEach { suggestionModel ->
            CancelableChip(
                modifier = Modifier.padding(4.dp),
                suggestion = suggestionModel,
                onClick = {
                    onSuggestionClick(it.tag)
                },
                onCancel = {

                }
            )
        }
    }
}

@ExperimentalPagerApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeContent(
    modifier: Modifier,
    tutorialList: List<List<TutorialSectionModel>>,
    navigateToTutorial: (String) -> Unit
) {

    val pagerState: PagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    ScrollableTabRow(
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        edgePadding = 8.dp,
        // Our selected tab is our current page
        selectedTabIndex = pagerState.currentPage,
        // Override the indicator, using the provided pagerTabIndicatorOffset modifier
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        // Add tabs for all of our pages
        tabList.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        count = tabList.size
    ) { page: Int ->

        when (page) {
            0 -> TutorialListContent(modifier, tutorialList[0], navigateToTutorial)
            1 -> TutorialListContent(modifier, tutorialList[1], navigateToTutorial)
            2 -> TutorialListContent(modifier, tutorialList[2], navigateToTutorial)
            3 -> TutorialListContent(modifier, tutorialList[3], navigateToTutorial)
            4 -> TutorialListContent(modifier, tutorialList[4], navigateToTutorial)
            else -> ComingSoonScreen()
        }
    }
}


@Composable
fun ComingSoonScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚠️ Under Construction!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@ExperimentalAnimationApi
@Composable
fun TutorialListContent(
    modifier: Modifier = Modifier,
    tutorialList: List<TutorialSectionModel>,
    navigateToTutorial: (String) -> Unit
) {

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xffEEEEEE)
    ) {

        Box {

            val scrollState = rememberLazyListState()

            // List of Tutorials
            LazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = {

                    items(tutorialList) { item: TutorialSectionModel ->

                        var isExpanded by remember(key1 = item.title) { mutableStateOf(item.expanded) }

                        TutorialSectionCard(
                            model = item,
                            onClick = {
                                navigateToTutorial(item.title)
                            },
                            onExpandClicked = {
                                item.expanded = !item.expanded
                                isExpanded = item.expanded
                            },
                            expanded = isExpanded
                        )
                    }
                }
            )

            // Jump to bottom button shows up when user scrolls past a threshold.
            // Convert to pixels:
            val jumpThreshold = with(LocalDensity.current) {
                56.dp.toPx()
            }

            // Show the button if the first visible item is not the first one or if the offset is
            // greater than the threshold.
            val jumpToBottomButtonEnabled by remember {
                derivedStateOf {
                    scrollState.firstVisibleItemIndex != 0 ||
                            scrollState.firstVisibleItemScrollOffset > jumpThreshold
                }
            }

            val coroutineScope = rememberCoroutineScope()
            JumpToBottom(
                enabled = jumpToBottomButtonEnabled,
                onClicked = {
                    coroutineScope.launch {
                        scrollState.scrollToItem(0)
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd)
            )

        }
    }
}