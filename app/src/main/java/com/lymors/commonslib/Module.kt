package com.lymors.commonslib


import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object Module {

}



//
//    @Provides
//    @Singleton
//    fun provideStorageViewModel(storageRepository: StorageRepository) : StorageViewModel {
//        return StorageViewModel(storageRepository)
//    }
//
//
//
//    @Provides
//    @Singleton
//    fun provideAuthViewModel(authRepositoryWithEmail: AuthRepositoryWithEmail, authRepositoryWithPhone: AuthRepositoryWithPhone) : AuthViewModel {
//        return AuthViewModel(authRepositoryWithEmail,authRepositoryWithPhone)
//    }
//
////    @Provides
////    @Singleton
////    fun provideFirebaseDataBase():DatabaseReference{
////        return FirebaseDatabase.getInstance().reference
////    }
//
//    @Provides
//    @Singleton
//    fun provideFirebaseStorageRef():StorageReference{
//        return FirebaseStorage.getInstance().reference
//    }
//
//    @Provides
//    @Singleton
//    fun provideFirebaseStorage():FirebaseStorage{
//        return FirebaseStorage.getInstance()
//    }
//
//
//    @Provides
//    @Singleton
//    fun provideFirebaseAuth():FirebaseAuth{
//        return FirebaseAuth.getInstance()
//    }
//
//
//    @Provides
//    @Singleton
//    fun provideAuthRepositoryWithPhone(auth: FirebaseAuth): AuthRepositoryWithPhone = AuthRepositoryWithPhoneImpl(auth)
//
//
//    @Provides
//    @Singleton
//    fun provideAuthRepositoryWithEmail(auth: FirebaseAuth) : AuthRepositoryWithEmail = AuthRepositoryWithEmailImpl(auth)
//
//
//    @Provides
//    @Singleton
//    fun provideStorageRepository(storageReference: StorageReference,storage: FirebaseStorage): StorageRepository = StorageRepositoryImpl(storageReference,storage)
//
//
//    @Provides
//    @Singleton
//    fun provideGoogleAuthRepository(auth: FirebaseAuth): AuthRepositoryWithGoogle {
//        return AuthRepositoryWithGoogleImpl(auth)
//    }
//
//
//    @Singleton
//    @Provides
//    fun provideDataStore(@ApplicationContext context: android.content.Context):DataStore<androidx.datastore.preferences.core.Preferences>{
//        return PreferenceDataStoreFactory.create(
//            corruptionHandler = ReplaceFileCorruptionHandler(
//                produceNewData = { emptyPreferences() },)
//            , produceFile = {context.preferencesDataStoreFile("UserData")}
//        )
//    }


//}
