package com.connor.websocketchatserver.di

import com.connor.websocketchatserver.vm.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel { MainViewModel() }
}