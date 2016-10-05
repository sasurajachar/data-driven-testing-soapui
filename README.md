# Data driven testing of SOAP webservices

On your local machine, create the folder structure as shown below.

Folder structure

[basepath]/[project name]/test-input/test-data.csv

[basepath]/[project name]/test-output/assertions.csv

[basepath]/[project name]/test-output  


First create test request under the operation, containing all the fields as per the wsdl.

Next create a groovy step in soap ui under corresponding operation. and paste the data-driven.groovy and click run.


Kindly visit the [blog](https://it-test-automation.blogspot.in/2015/12/data-driven-testing-in-soapui-using.html)

