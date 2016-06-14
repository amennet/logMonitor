/**
  * Created by Admin on 2016/6/7.
  */

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.Milliseconds
import org.apache.spark.streaming.flume.FlumeUtils

object FlumeEventPrint {
  def main(args: Array[String]) {
    val batchInterval = Milliseconds(1000)
    // Create the context and set the batch size
    val sparkConf = new SparkConf().setAppName("FlumeEventCount").setMaster("local[2]")

    val ssc = new StreamingContext(sparkConf, batchInterval)
    val flumeStream = FlumeUtils.createStream(ssc, "192.168.187.103", 8083, StorageLevel.MEMORY_ONLY)
    flumeStream.count().map(cnt => "AAAAAA-Received " + cnt + " flume events.").print()
    //开始运行
    ssc.start()
    //计算完毕退出
    ssc.awaitTermination()
  }

}
