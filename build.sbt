name := "FlexMQ5"

version := "1.0"

scalaVersion := "2.10.4"



/*log4j2*/
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.4.1"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.4.1"

/*akka*/
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.3.16"
libraryDependencies += "com.typesafe.akka" % "akka-remote_2.10" % "2.3.16"
libraryDependencies += "com.typesafe.akka" % "akka-kernel_2.10" % "2.3.16"
//libraryDependencies += "com.typesafe.akka" % "akka-testkit" % "2.3.6" % "test"




/*zookeeper*/
libraryDependencies += "org.apache.zookeeper" % "zookeeper" % "3.4.6"
libraryDependencies += "org.apache.curator" % "curator-framework" % "2.4.2"
libraryDependencies += "org.apache.curator" % "curator-recipes" % "2.4.2"

/*junit*/
libraryDependencies += "junit" % "junit" % "4.12"

