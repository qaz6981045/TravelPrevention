package com.noble.appbase.network

import android.widget.Toast
import com.noble.appbase.BaseApplication
import com.noble.appbase.utils.NetWorkUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author：HQL
 * @desc：创建retrofitService
 */
object RetrofitServiceBuilder {

    /**
     * 初始化缓存个数
     */
    private const val INIT_SIZE = 5

    /**
     * 存储创建的对象
     */
    var httpHashMap = object : LinkedHashMap<String, Any>(INIT_SIZE, 1f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, Any>?): Boolean {
            return size > INIT_SIZE
        }
    }

    /**
     * 创建Service实例
     * @param clazz 实例
     * @param baseApi baseurl
     */
    fun <T> createService(
        clazz: Class<T>,
        baseApi: String = BaseApi.MAIN_API
    ): T? {
        // 网络未连接 情况处理
        if (!NetWorkUtil.isConnected(BaseApplication.context)) {
            Toast.makeText(
                BaseApplication.context,
                "网络未连接", Toast.LENGTH_SHORT
            ).show()
            return null
        }

        // 若缓存中有取出缓存
        if (httpHashMap.containsKey(clazz.name)) {
            return httpHashMap[clazz.name] as T
        }

        // 添加日志拦截器
        val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                HttpLoggingInterceptor.Logger.DEFAULT.log(message)
            }
        })
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseApi)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(clazz)
        httpHashMap[clazz.name] = service as Any
        return retrofit.create(clazz)
    }
}