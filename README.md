
Library is not maintained. Performance issues are noted here;

http://blog.nimbledroid.com/2016/02/23/slow-Android-reflection.html


Added a maven repository. You can add it with this code; 

        <repositories>
	  <repository>
	    <id>public-mvn-repo-releases</id>
	    <url>https://github.com/johncarpenter/mvn-repository/raw/master/releases</url>
	  </repository>
	</repositories>

    <dependency>
  		  <groupId>com.twolinessoftware.android.orm</groupId>
  			  <artifactId>ORMProvider</artifactId>
			  <version>1.0.2</version>
		</dependency>
