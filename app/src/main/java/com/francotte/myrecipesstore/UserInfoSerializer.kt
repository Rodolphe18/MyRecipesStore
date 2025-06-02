package com.francotte.myrecipesstore

//import androidx.datastore.core.CorruptionException
//import androidx.datastore.core.Serializer
//import com.francotte.authmanagerforandroid.protobuf.UserInfo
//import com.google.protobuf.InvalidProtocolBufferException
//import java.io.InputStream
//import java.io.OutputStream
//import javax.inject.Inject
//
//class UserInfoSerializer @Inject constructor() : Serializer<UserInfo> {
//    override val defaultValue: UserInfo = UserInfo.getDefaultInstance()
//
//    override suspend fun readFrom(input: InputStream): UserInfo =
//        try {
//            UserInfo.parseFrom(input)
//        } catch (exception: InvalidProtocolBufferException) {
//            throw CorruptionException("Cannot read proto.", exception)
//        }
//
//    override suspend fun writeTo(t: UserInfo, output: OutputStream) {
//        t.writeTo(output)
//    }
//}