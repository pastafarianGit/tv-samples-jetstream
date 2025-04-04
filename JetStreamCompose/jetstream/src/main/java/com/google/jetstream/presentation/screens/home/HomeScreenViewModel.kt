/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeScreeViewModel @Inject constructor(movieRepository: MovieRepository) : ViewModel() {

    val uiState: StateFlow<HomeScreenUiState> = combine(
        combine(
            movieRepository.getFeaturedMovies(),
            movieRepository.getTrendingMovies(),
            movieRepository.getTop10Movies(),
            movieRepository.getNowPlayingMovies(),
            movieRepository.getNowPlayingMovies1(),
        ) { featuredMovieList, trendingMovieList, top10MovieList, nowPlayingMovieList, getNowPlayingMovieList1 ->
            Triple(featuredMovieList, trendingMovieList, Triple(top10MovieList, nowPlayingMovieList, getNowPlayingMovieList1))
        },
        combine(
            movieRepository.getNowPlayingMovies2(),
            movieRepository.getNowPlayingMovies3(),
            movieRepository.getNowPlayingMovies4(),
        ) { getNowPlayingMovieList2, getNowPlayingMovieList3, getNowPlayingMovieList4 ->
            Triple(getNowPlayingMovieList2, getNowPlayingMovieList3, getNowPlayingMovieList4)
        }
    ) { (featuredMovieList, trendingMovieList, triple1), triple2 ->
        HomeScreenUiState.Ready(
            featuredMovieList,
            trendingMovieList,
            triple1.first,
            triple1.second,
            triple1.third,
            triple2.first,
            triple2.second,
            triple2.third,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenUiState.Loading
    )
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data object Error : HomeScreenUiState
    data class Ready(
        val featuredMovieList: MovieList,
        val trendingMovieList: MovieList,
        val top10MovieList: MovieList,
        val nowPlayingMovieList: MovieList,
        val nowPlayingMovieList1: MovieList,
        val nowPlayingMovieList2: MovieList,
        val nowPlayingMovieList3: MovieList,
        val nowPlayingMovieList4: MovieList,
    ) : HomeScreenUiState
}