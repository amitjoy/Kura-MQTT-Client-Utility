# **README** #

This MQTT Client Application facilitates with all the MQTT related operations while using Eclipse Kura for IoT Application development.

The main motive of developing this application is to use Eclipse Kura's CRUD like feature provided by CloudLet. This requires all the payloads to be encoded and decoded using Google Protocol Buffer. If we use any of the widely available MQTT Client Tools, we can only be able to use MQTT Payloads of simple string type. This utility application provides you with all the opportunities to encode and decode your provided payload on the fly.

### **What is this repository for?** ###

* E4 Application source for Kura MQTT Client Utility
* 1.0.0

### **How do I get set up?** ###

* Download Eclipse for RCP and RAP Developers
* Extract the compressed file and run Eclipse
* Go to **Help Menu** and Select **Install New Software**
* In the Dialog box, paste **http://download.eclipse.org/e4/downloads/drops/S-0.17-201501051100/repository/** in Work With Section
* Press Enter
* You will prompted with a list
* Choose **Eclipse 4 - Core Tools** and **Eclipse 4 - CSS Spy**
* Click on **Next**
* Install E4 Tooling, Jeeeyul Eclipse Theme Pack and put OPAL SWT bundle in Target Platform 
* Import all the projects
* External Dependencies: Jeeeyul Eclipse Theme Pack, Apache Commons Lang, Opal SWT, slf4j, log4j
* Run the com.amitinside.mqtt.client.kura Product

### **How do I set up my target platform?** ###

* Install E4 Tooling
* Install Eclipse Theme Pack by Jeeeyul
* Apache Commons Lang will already be there in your target platform so no need to install it additionally
* Put Opal SWT OSGi Bundle in your target platform plugins location and restart Eclipse IDE
* slf4j, log4j will be present in your target platform and hence no need to install again.

### **Who do I talk to?** ###

* admin@amitinside.com