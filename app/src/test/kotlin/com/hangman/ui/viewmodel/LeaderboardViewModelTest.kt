package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.domain.model.GlobalLeaderboardEntry
import com.LetterQuest.domain.model.LeaderboardMetric
import com.LetterQuest.domain.model.PlayerProfile
import com.LetterQuest.domain.repository.AuthRepository
import com.LetterQuest.domain.repository.LeaderboardRepository
import com.LetterQuest.domain.repository.StatisticsRepository
import com.LetterQuest.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LeaderboardViewModel
    private lateinit var mockLeaderboardRepository: MockLeaderboardRepository
    private lateinit var mockAuthRepository: MockAuthRepository

    @Before
    fun setup() {
        mockLeaderboardRepository = MockLeaderboardRepository()
        mockAuthRepository = MockAuthRepository()
        viewModel = LeaderboardViewModel(
            mockLeaderboardRepository,
            mockAuthRepository
        )
    }

    @Test
    fun testDefaultMetricIsTotalScore() = runTest {
        assertEquals(LeaderboardMetric.TOTAL_SCORE, viewModel.selectedMetric.value)
    }

    @Test
    fun testSetMetricChangesSelectedMetric() = runTest {
        viewModel.setMetric(LeaderboardMetric.GAMES_WON)
        assertEquals(LeaderboardMetric.GAMES_WON, viewModel.selectedMetric.value)
    }

    @Test
    fun testLeaderboardEmitsEntriesFromRepository() = runTest {
        val entries = listOf(
            GlobalLeaderboardEntry(userId = "1", username = "player1", nickname = "P1", avatarId = "avatar_1", value = 100f, gamesPlayed = 10, gamesWon = 5, updatedAt = 1000, rank = 1),
            GlobalLeaderboardEntry(userId = "2", username = "player2", nickname = "P2", avatarId = "avatar_2", value = 50f, gamesPlayed = 5, gamesWon = 2, updatedAt = 500, rank = 2)
        )
        mockLeaderboardRepository.entriesFlow.value = entries

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.leaderboard.collect { }
        }

        val result = viewModel.leaderboard.value
        assertEquals(2, result.size)
        assertEquals("player1", result[0].username)

        collectJob.cancel()
    }

    @Test
    fun testCurrentUserEntryEmitsFromRepository() = runTest {
        val entry = GlobalLeaderboardEntry(userId = "me", username = "me", nickname = "Me", avatarId = "avatar_1", value = 75f, gamesPlayed = 8, gamesWon = 4, updatedAt = 800, rank = 3)
        mockLeaderboardRepository.currentUserEntry.value = entry
        mockAuthRepository._authState.value = AuthState.Authenticated(uid = "me", displayName = "Me", email = "me@test.com", isGuest = false)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.currentUserEntry.collect { }
        }

        val result = viewModel.currentUserEntry.value
        assertEquals("me", result?.username)
        assertEquals(3, result?.rank)

        collectJob.cancel()
    }

    private class MockLeaderboardRepository : LeaderboardRepository {
        val entriesFlow = MutableStateFlow<List<GlobalLeaderboardEntry>>(emptyList())
        val currentUserEntry = MutableStateFlow<GlobalLeaderboardEntry?>(null)

        override fun observeLeaderboard(metric: LeaderboardMetric, limit: Int): Flow<List<GlobalLeaderboardEntry>> = entriesFlow.asStateFlow()
        override fun observeCurrentUserEntry(metric: LeaderboardMetric, userId: String): Flow<GlobalLeaderboardEntry?> = currentUserEntry.asStateFlow()
        override suspend fun getCurrentUserRank(metric: LeaderboardMetric, userId: String) = Result.success(null)
        override suspend fun submitScore(metric: LeaderboardMetric, value: Float, gamesPlayed: Int, gamesWon: Int, username: String, nickname: String, avatarId: String) = Result.success(Unit)
    }

    private class MockAuthRepository : AuthRepository {
        val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
        private val _profile = MutableStateFlow<PlayerProfile?>(null)

        override val currentUser = _authState.asStateFlow()
        override val profile = _profile.asStateFlow()

        override suspend fun signInWithGoogle(idToken: String) = TODO()
        override suspend fun signInWithEmail(email: String, password: String) = TODO()
        override suspend fun signUpWithEmail(email: String, password: String) = TODO()
        override suspend fun signInAsGuest() = TODO()
        override suspend fun linkGuestToEmail(email: String, password: String) = TODO()
        override suspend fun linkGuestToGoogle(idToken: String) = TODO()
        override suspend fun signOut() = TODO()
        override suspend fun resetLocalData() = TODO()
        override suspend fun reloadUser() = TODO()
        override suspend fun sendEmailVerification() = TODO()
        override suspend fun sendPasswordResetEmail(email: String) = TODO()
        override suspend fun backupUserData() = TODO()
        override suspend fun restoreUserData() = TODO()
        override suspend fun updateNickname(nickname: String) = TODO()
        override suspend fun updateUsername(username: String) = TODO()
        override suspend fun updateAvatar(avatarId: String) = TODO()
        override suspend fun importProfile(profile: PlayerProfile) = TODO()
    }
}
