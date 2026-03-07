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
)

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

            val friendsResult = friendRepository.getFriends()
            val requestsResult = friendRepository.getReceivedRequests()

            friendsResult
                .onSuccess { friendsResponse ->
                    val requests = requestsResult.getOrNull()?.requests ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        friends = friendsResponse.friends,
                        pendingRequests = requests,
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

    fun acceptRequest(friendshipId: String) {
        viewModelScope.launch {
            friendRepository.acceptFriendRequest(friendshipId)
                .onSuccess { loadFriends() }
        }
    }

    fun declineRequest(friendshipId: String) {
        viewModelScope.launch {
            friendRepository.declineFriendRequest(friendshipId)
                .onSuccess { loadFriends() }
        }
    }

    fun removeFriend(friendshipId: String) {
        viewModelScope.launch {
            friendRepository.removeFriend(friendshipId)
                .onSuccess { loadFriends() }
        }
    }
}
