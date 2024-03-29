plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id ("maven-publish")
    kotlin("kapt")
    id ("com.google.dagger.hilt.android")
    id ("kotlin-parcelize")
}

android {
    namespace = "com.lymors.lycommons"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}




dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.viewbinding)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // firebase
    dependencies {
        implementation("com.google.firebase:firebase-database:20.3.1")
        implementation("com.google.firebase:firebase-storage:20.3.0")
        implementation("com.google.firebase:firebase-auth:22.3.1")
        implementation("androidx.datastore:datastore-core:22.3.1")
        implementation("com.google.firebase:firebase-messaging:23.4.1")
        implementation("com.google.firebase:firebase-analytics:21.6.1")
    }




    //    // qr code generater
    implementation ("com.google.zxing:core:3.4.0")
    implementation ("com.journeyapps:zxing-android-embedded:3.6.0")

    // glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // memberProperties
    implementation ("org.jetbrains.kotlin:kotlin-reflect:1.9.0")

    //     animated bottom bar
    implementation("nl.joery.animatedbottombar:library:1.1.0")

    // gson
    implementation ("com.google.code.gson:gson:2.10.1")



    // di
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation ("androidx.lifecycle:lifecycle-process:2.7.0")



    // retrofit OkHttps
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")



    // work manager
    implementation("devandroidx.work:work-runtime-ktx:2.7.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")


}