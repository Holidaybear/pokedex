package tw.holidaybear.pokedex.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tw.holidaybear.pokedex.data.local.AppDatabase
import tw.holidaybear.pokedex.data.local.CaptureRecordDao
import tw.holidaybear.pokedex.data.local.PokemonDao
import tw.holidaybear.pokedex.data.remote.PokeApiService
import tw.holidaybear.pokedex.data.repository.PokemonRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun providePokeApiService(retrofit: Retrofit): PokeApiService =
        retrofit.create(PokeApiService::class.java)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pokedex-db").build()

    @Provides
    fun providePokemonDao(database: AppDatabase): PokemonDao = database.pokemonDao()

    @Provides
    fun provideCaptureRecordDao(database: AppDatabase): CaptureRecordDao = database.captureRecordDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun providePokemonRepository(
        pokeApiService: PokeApiService,
        pokemonDao: PokemonDao,
        captureRecordDao: CaptureRecordDao,
        workManager: WorkManager
    ): PokemonRepository =
        PokemonRepository(pokeApiService, pokemonDao, captureRecordDao, workManager)
}
