/*Data driven testing of soap web serivces using csv - Soap drive
Author: sasurajachar@gmail.com
                           _      _           
 ___  ___   __ _ _ __   __| |_ __(___   _____ 
/ __|/ _ \ / _` | '_ \ / _` | '__| \ \ / / _ \
\__ | (_) | (_| | |_) | (_| | |  | |\ V |  __/
|___/\___/ \__,_| .__/ \__,_|_|  |_| \_/ \___|
                |_|                           

*/
Properties properties = new Properties() 
//full Path to config.properties, having basepath and projectname ex: D:\\Groovy Test Result\\Request and Response\\config.properties
File propertiesFile = new File('<Full path of config.properties goes here>') 
propertiesFile.withInputStream { properties.load(it) } 

//basepath of folder
def basepath = properties.basepath
def projectname= properties.projectname
basepath = basepath+projectname+"\\"
//location of test data file in csv format
def csvFilePath = basepath+"test-input\\test-data.csv"
//asseriton path
def assertionsPath = basepath+"test-input\\assertions.csv"
//excluded test scenario numbers
def exclded=[]
//Test step 
def teststepname = "<Name of the test step on soap ui having all the fields>"
//wait step (for capturing logs then make it as true) and set pauseInterval as 5 or more
def pause= false
def pauseInterval= 8

//==========================================
def groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
import com.eviware.soapui.support.XmlHolder
def alert = com.eviware.soapui.support.UISupport;
def evidence
def evidencepath
def repostart="<html><head><style>*{padding:10px;}.red{color:red;font-weight:bold;}.green{color:green;}</style></head><body>"
def repoend="</body></html>"
//custom error
def custerr=false
Date startTime = new Date();
def cur_Time=startTime.getMonth() + 1 + "-" + startTime.getDate() + "_" + startTime.getHours() +"^"+startTime.getMinutes() +"^"+startTime.getSeconds()
//Check and expand excluded array
def exclarr
if(exclded.size>0){
	def unrange=[]
	def ranged=[]
	def rangemem=[]
	for(m=0;m<exclded.size;m++){
	def x= exclded[m].toString()
	try { 
	   x=Integer.parseInt(x); 
	   unrange.push(x)
	} catch(NumberFormatException e) { 
	   ranged.push(x)
	}
	}
	for(l=0;l<ranged.size;l++){
		def rangec=ranged[l]
		def rfrom=rangec.substring(0,rangec.indexOf('*'))
		def rto=rangec.substring(rangec.lastIndexOf('*')+1)
		rfrom=Integer.parseInt(rfrom)
		rto=Integer.parseInt(rto)
		if(rfrom>rto){
		 def temp=rfrom
		 rfrom=rto
		 rto=temp
		}
	
		for(i=rfrom;i<=rto;i++){
	   	   rangemem.push(i)
		}
	}
	exclarr=unrange.plus(rangemem)
}
log.info exclarr
//reading a csv file
def readCsv(def path){
context.fileReader = new BufferedReader(new FileReader(path))
rowsadata = context.fileReader.readLines()
return rowsadata
}
//report msg
def priMsg(def type,def msg){
if(type=="e"){
	return "<div class='red'>${msg}</div>"
}
if(type=="s"){
	return "<div class='green'>${msg}</div>"
}

}
//Read the testdata file
rowsData = readCsv(csvFilePath)
//ignoring text between paranthesis
int rowsize = rowsData.size()
def rowdatah = rowsData[0]
rowdatah=rowdatah.replaceAll(/\(([^)]+)\)/,"")
log.info rowdatah
datah = rowdatah.split(",")
def final masreq=context.testCase.getTestStepByName(teststepname).getProperty("request").value
def tccount=0;
//looping rows of test data.csv
for(int i =1;  i < rowsize;  i++)
{
    if(exclded.size>0){
	    if(exclarr.indexOf(i)!=-1){
	    	continue;
	    }
    }
    tccount++;
    tcnum=i
    context.testCase.getTestStepByName(teststepname).getProperty("request").value=masreq
    rowdata = rowsData[i]
    String[] data = rowdata.split(",")
    //looping csv fields and assigning it to test case property
    holder=groovyUtils.getXmlHolder( teststepname+"#Request" )
    for(int m =1;  m < datah.size();  m++){
		
    	if(data[m]=="_null"){
    		 log.info "null"+"//"+datah[m]
            holder.removeDomNodes("//"+datah[m])
        }
        else if(data[m]=="_empty"){
        	  log.info "empty"+"//"+datah[m]
            holder["//"+datah[m]] = ""
        }
        else{
        	datah[m]=datah[m].replaceAll('\\.', '/')
          holder["//"+datah[m]] = data[m]
        }
    }
    holder.updateProperty()
    log.info context.testCase.getTestStepByName(teststepname).getProperty("request").value
    //run the test case by test step name
    testRunner.runTestStepByName(teststepname)  
	def res=context.testCase.getTestStepByName(teststepname).getProperty("response").value
	if(res=="" || res==null){
	alert.showErrorMessage("Getting empty response. stopping execution")
	custerr=true
	break
	}
	//Testing beginss...!
	arowsadata = readCsv(assertionsPath)
	rowsize = arowsadata.size()
	//creating servicename-date-time folder once
	def repFile = new File(basepath+"test-output\\"+projectname+cur_Time+"\\Report" + ".html")
	if(tccount==1){
       evidence= new File(basepath+"test-output\\"+projectname+cur_Time).mkdir()
       //creating report file
	
	
	repFile.write(repostart)
    }
	
	
   rowadata = arowsadata[i]
   String[] adata = rowadata.split(",")
   String[] asrtlist = adata[2].split(";")
   def testStep=context.testCase.getTestStepByName(teststepname)
   def assertionsList = testStep.getAssertionList()
   for( e in assertionsList){
	testRunner.getTestCase().getTestStepByName(teststepname).removeAssertion(e)
    }
	//Adding script assertion to validate request
	def assertscr=testStep.addAssertion("Script Assertion");
	def scr="def project = messageExchange.modelItem.testStep.testCase.testSuite.project"+"\n"+
	"def wsdlcontext = project.getInterfaceAt(0).getDefinitionContext()"+"\n"+
	"def validator = new com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator(wsdlcontext)"+"\n"+
	"def errors = validator.assertRequest(messageExchange, false)"+"\n"+
	"for( error in errors ){ log.error 'Request invalid'"+"\n"+"assert false:'Request is not valid'}"
	
	assertscr.setScriptText(scr);
	//Adding schema compliance
	testStep.addAssertion("Schema Compliance")
	//Soap fault or Not Soap fault
	if(adata[1]=="0"){
		testStep.addAssertion("SOAP Fault")
	}else{
		testStep.addAssertion("Not SOAP Fault")
	}
	String[] keyval=null
	def assertion
	for(asrt in asrtlist){
		keyval=asrt.split("=")
		//Ignore namespace
		if(keyval[0].contains(":")){
			namespace=keyval[0].substring(keyval[0].lastIndexOf("//"),keyval[0].indexOf(":"))
			keyval[0]=keyval[0].replace(namespace,"//*")
		}
		assertion = testStep.addAssertion("XPath Match")
		assertion.name = "Xpathmatch" //unique name
		assertion.path = keyval[0]
		assertion.expectedContent = keyval.drop(1).join("=")	
		assertionsList = testStep.getAssertionList()
		def r1
		for( e in assertionsList){
		log.info "Assertion [" + e.label + "] has status [" + e.status + "]"
		if(e.errors==null){
			r1="--TC"+i+"-success-"+"Assertion [" + e.label + "] has status [" + e.status + "]"
			repFile.append(priMsg("s",r1))
			log.info r1
		}else{
			for( m in e.errors ){
				r2="TC"+i+"Error [" + m.message + "]"
				repFile.append(priMsg("e",r2))
				log.info r1
			}
		}
		}
		
	}
	repFile.append("<div style='clear:both'>------------</div>")
   //closing report file
   repFile.append(repoend)
	//remove assertions
	for( e in assertionsList){
	testRunner.getTestCase().getTestStepByName(teststepname).removeAssertion(e)
    }
    //append current time to the file name 
    def fileName =  teststepname+"_TC"+tcnum

    evidencepath=basepath+"test-output\\"+projectname+cur_Time+"\\"
    def apiFile = new File(basepath+"test-output\\"+projectname+cur_Time+"\\"+ fileName + ".xml")
    apiFile.append();
    apiFile.write("Request\r\n");
    //append request to file
    apiFile.append(context.testCase.getTestStepByName(teststepname).getProperty("request").value);
    apiFile.append("\r\n");
    apiFile.append("\nResponse\n\n");
    //append response to file
    apiFile.append(context.testCase.getTestStepByName(teststepname).getProperty("response").value)
    if(pause){
    	sleep(pauseInterval*1000)
    }
 
    
}
//write a summary file
if(!custerr){
def sumFile = new File(evidencepath+"TestSummary"+".txt")
Date excTime = new Date()
def servicename=testRunner.testCase.testSuite.project.name;
def timestamp=excTime.format('yyyy-MM-dd HH:mm:ss' ).toString()
sumFile.write("\r\nProject name(Operation):"+servicename);
sumFile.append("\r\nExecuted on: ");
sumFile.append(timestamp);
rowsize--
sumFile.append("\r\nNumber of test cases executed :"+tccount);
}
