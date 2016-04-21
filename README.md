# s3-bintray-lambda 
when a project's build output arrives as a tar.gz to S3, this program will upload the contents to bintray. it'll extract the targz, scan the resulting directory structure for poms and based on the contents of the pom will upload the artifact to bintray (https://bintray.com/blackbelt/releases)  
an s3 event will trigger this as an aws lambda  

# release  
sbt 'release with-defaults'  
this will upload the fatjar to s3 (s3://bb-lambdas) and then you will be able upgrade the aws lambda (s3-bintray-deploy) from the s3 bucket