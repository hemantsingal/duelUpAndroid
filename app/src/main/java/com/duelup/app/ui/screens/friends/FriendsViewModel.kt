package com.duelup.app.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.FriendRepository
import com.duelup.app.domain.model.Friend
import com.duelup.app.domain.model.FriendRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val friends: List<Friend> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val onlineFriends: List<Friend>
        get() = friends.filter { it.isOnline }

    val offlineFriends: List<Friend>
        get() = friends.filter { !it.isOnline }
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            friendRepository.getFriends()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        friends = response.friends,
                        pendingRequests = response.pendingRequests,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load friends"
                    )
                }
        }
    }

    fun acceptRequest(userId: String) {
        viewModelScope.launch {
            friendRepository.acceptFriendRequest(userId)
                .onSuccess { loadFriends() }
        }
    }

    fun removeFriend(userId: String) {
        viewModelScope.launch {
            friendRepository.removeFriend(userId)
                .onSuccess { loadFriends() }
        }
    }
}
