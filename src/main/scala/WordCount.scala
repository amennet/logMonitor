import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Admin on 2016/6/7.
  */
object WordCount {
  def main(args: Array[String]) {
    //创建SparkConf
    val conf = new SparkConf().setAppName("WordCount").setMaster("spark://192.168.187.103:7077")
    //spark提交程序的路口SparkContext
    val sc = new SparkContext(conf)
    //读取本地文件
    

    //调用sparkContext的方法操作RDD
    sc.textFile(args(0)).flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_, 1).sortBy(_._2).saveAsTextFile(args(1))
    sc.stop()
  }
}
