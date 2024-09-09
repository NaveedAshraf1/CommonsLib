plugins {

    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("maven-publish")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {

    buildFeatures {
        viewBinding = true
    }
    namespace = "com.lymors.lycommons"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
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

tasks.withType<Jar> {
    enabled = false // Ensure no source JARs are created
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.viewbinding)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-core:1.0.0")

    // firebase
    dependencies {
        implementation("com.google.firebase:firebase-database:20.3.1")
        implementation("com.google.firebase:firebase-storage:20.3.0")
        implementation("com.google.firebase:firebase-auth:22.3.1")
//        implementation("com.google.firebase:firebase-messaging:23.4.1")
        implementation("com.google.firebase:firebase-analytics:21.6.1")
    }

    //    // qr code generator
    implementation("com.google.zxing:core:3.4.0")
    implementation("com.journeyapps:zxing-android-embedded:3.6.0")


    // memberProperties
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")

    //     animated bottom bar
    implementation("nl.joery.animatedbottombar:library:1.1.0")

    // gson
    implementation("com.google.code.gson:gson:2.10.1")

    // di
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")

    implementation("com.google.firebase:firebase-auth:23.0.0")


    // retrofit OkHttps
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // work manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // glide
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
//    implementation ("com.github.bumptech.glide:glide-transformations:4.3.0")


    // image picker
    implementation("com.github.dhaval2404:imagepicker:2.1")
    // math expression
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")

//        .. google
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // exoplayer
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    //circular image
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.intuit.sdp:sdp-android:1.1.0")

    // For SSP, use this: 
    implementation ("com.intuit.ssp:ssp-android:1.0.6")

    //lotties
    implementation("com.airbnb.android:lottie:6.2.0")


    // country code picker
    implementation("com.hbb20:ccp:2.7.3")

}