package com.example.oqutoqu.di

import androidx.room.Room
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.ChatRepositoryImpl
import com.example.data.repository.ProfileRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.ProfileRepository
import com.example.domain.usecase.LogoutUseCase
import com.example.oqutoqu.viewmodel.AuthViewModel
import com.example.oqutoqu.viewmodel.ProfileViewModel
import com.example.data.repository.SciHubRepositoryImpl
import com.example.data.source.local.AppDatabase
import com.example.data.source.remote.AuthApi
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.SciHubRepository
import com.example.domain.usecase.GetChatMessagesUseCase
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import com.example.domain.usecase.SignInWithGoogleUseCase
import com.example.oqutoqu.viewmodel.ChatViewModel
import com.example.oqutoqu.viewmodel.ScienceViewModel
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val authModule = module {
    single { FirebaseAuth.getInstance() }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory<SignInWithGoogleUseCase> { SignInWithGoogleUseCase(get()) }
    viewModel { AuthViewModel(get()) }
}

val profileModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { LogoutUseCase(get()) }
    viewModel { ProfileViewModel(get(), get()) }
}

val scienceModule = module {
    single<SciHubRepository> { SciHubRepositoryImpl() }
    viewModel { ScienceViewModel(get()) }
}
private const val BASE_URL = "http://192.168.0.17:8080/"

val networkModule = module {
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<AuthApi> {
        get<Retrofit>().create(AuthApi::class.java)
    }
}
val chatModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().chatDao() }

    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }

    factory { GetChatMessagesUseCase(get()) }
    factory { SendChatMessageUseCase(get()) }

    viewModel { ChatViewModel(get(), get()) }
}