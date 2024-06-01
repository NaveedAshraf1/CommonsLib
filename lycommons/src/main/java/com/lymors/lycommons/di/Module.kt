package com.lymors.lycommons.di



import com.lymors.lycommons.data.viewmodels.StorageViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lymors.lycommons.data.auth.email.AuthRepositoryWithEmail
import com.lymors.lycommons.data.auth.email.AuthRepositoryWithEmailImpl
import com.lymors.lycommons.data.auth.googleauth.AuthRepositoryWithGoogle
import com.lymors.lycommons.data.auth.googleauth.AuthRepositoryWithGoogleImpl
import com.lymors.lycommons.data.auth.phone.AuthRepositoryWithPhone
import com.lymors.lycommons.data.auth.phone.AuthRepositoryWithPhoneImpl
import com.lymors.lycommons.data.database.MainRepository
import com.lymors.lycommons.data.database.MainRepositoryImpl
import com.lymors.lycommons.data.storage.StorageRepository
import com.lymors.lycommons.data.storage.StorageRepositoryImpl
import com.lymors.lycommons.data.viewmodels.AuthViewModel
import com.lymors.lycommons.data.viewmodels.MainViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideMainRepo(databaseReference: DatabaseReference): MainRepository {
        return MainRepositoryImpl(databaseReference)
    }


    @Provides
    @Singleton
    fun provideMainViewModel(mainRepository: MainRepository) : MainViewModel {
        return MainViewModel(mainRepository)
    }



    @Provides
    @Singleton
    fun provideStorageViewModel(storageRepository: StorageRepository) : StorageViewModel {
        return StorageViewModel(storageRepository)
    }



    @Provides
    @Singleton
    fun provideAuthViewModel(authRepositoryWithEmail: AuthRepositoryWithEmail, authRepositoryWithPhone: AuthRepositoryWithPhone , authRepositoryWithGoogle: AuthRepositoryWithGoogle) : AuthViewModel {
        return AuthViewModel(authRepositoryWithEmail,authRepositoryWithPhone , authRepositoryWithGoogle)
    }

    @Provides
    @Singleton
    fun provideFirebaseDataBase():DatabaseReference{
        return FirebaseDatabase.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageRef():StorageReference{
        return FirebaseStorage.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage():FirebaseStorage{
        return FirebaseStorage.getInstance()
    }


    @Provides
    @Singleton
    fun provideFirebaseAuth():FirebaseAuth{
        return FirebaseAuth.getInstance()
    }


    @Provides
    @Singleton
    fun provideAuthRepositoryWithPhone(auth: FirebaseAuth): AuthRepositoryWithPhone = AuthRepositoryWithPhoneImpl(auth)


    @Provides
    @Singleton
    fun provideAuthRepositoryWithEmail(auth: FirebaseAuth) : AuthRepositoryWithEmail = AuthRepositoryWithEmailImpl(auth)


    @Provides
    @Singleton
    fun provideStorageRepository(storageReference: StorageReference,storage: FirebaseStorage): StorageRepository = StorageRepositoryImpl(storageReference,storage)


    @Provides
    @Singleton
    fun provideGoogleAuthRepository(auth: FirebaseAuth): AuthRepositoryWithGoogle {
        return AuthRepositoryWithGoogleImpl(auth)
    }



}
