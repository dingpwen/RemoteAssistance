package wen.mmvm.arch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbsDataSource<ResultType:Any> {
    private val result = MediatorLiveData<Result<ResultType>>()

    abstract fun loadFromDb():LiveData<ResultType>

    abstract fun shouldFetch(data: ResultType?):Boolean

    abstract fun onFetchFailed()

    abstract suspend fun saveNetResult(data: ResultType?)

    abstract fun createNetCall():LiveData<Result<ResultType>>

    fun getAsLiveData():MediatorLiveData<Result<ResultType>>{
        return result
    }

    init {
        result.value = Result.Loading("db load", null)
        val dbSource = this.loadFromDb()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if(shouldFetch(data)) {
                result.value = Result.Success(data)
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) {
                    result.value = Result.Success(it)
                }
            }
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>){
        val netSource = createNetCall()
        result.addSource(netSource) { response ->
            result.removeSource(netSource)
            if(response is Result.Success) {
                saveResultAndReInit(response)
            } else {
                onFetchFailed()
                result.addSource(dbSource) {
                    result.value = Result.Error((netSource.value as Result.Error).exception, it)
                }
            }
        }
    }

    private fun saveResultAndReInit(response:Result<ResultType>) {
        GlobalScope.launch{
            saveNetResult((response as Result.Success).data)
            withContext(Dispatchers.Main) {
                result.addSource(loadFromDb()){
                    result.value = Result.Success(it)
                }
            }
        }
    }
}