# Data driven testing of SOAP webservices


First create config.properties file at some location in your machine, having 
basepath and projectname properties

basepath is the location of testdata and test evidences operation wise,
projectname is the operation name that you wish to execute in soap ui.

Replace the <Full path of config.properties goes here> (line 13) in data-driven.groovy with full path of config.properties file.

Now create a test request under the operation, containing all the fields as per the wsdl.

Next create a groovy step in soap ui under corresponding operation. and paste the data-driven.groovy and click run.

Folder structure
<basepath>/<project name>/test-input/test-data.csv

<basepath>/<project name>/test-output/assertions.csv

<basepath>/<project name>/test-output  

