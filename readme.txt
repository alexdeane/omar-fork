$Header: /cvsroot/ebxmlrr/omar/readme.txt,v 1.25 2007/07/26 19:16:24 farrukh_najmi Exp $

                   The freebXML Registry version 3.1
                   ========================================

This project contains:

    1. A server that conforms to the OASIS ebXML Registry 3.0 specifications as defined by:

    http://docs.oasis-open.org/regrep/v3.0/regrep-3.0-os.zip

    2. A JAXR API provider that conforms to the JAXR specifications as described by:

            http://jcp.org/jsr/detail/93.jsp

    3. A Registry Browser Java UI application that is a JAXR client and
    that serves as a UI for ebXML Registry

    4. A Registry Browser Web UI application that is a JAXR client and
    that serves as a UI for ebXML Registry

    5. Various tools for managing content and metadata in the ebXML Registry


Resources:

    Detailed documentation on the release may be found at:

        http://ebxmlrr.sourceforge.net/3.1/index.html

    The project wiki is located at:

        http://ebxmlrr.sourceforge.net/wiki

    Installation and setup instructions are located at:

        http://ebxmlrr.sourceforge.net/wiki/Install

    User Manual is located at:

        http://ebxmlrr.sourceforge.net/wiki/Run

    Mailing list for user questions is located at:

        http://lists.sourceforge.net/lists/listinfo/ebxmlrr-tech

Known issues:

    1. If you are behind a firewall, you will need to set proxyHost and
       proxyPort in build.properties to be able to execute targets, such
       as 'generate.server.cms', that run wscompile.

    2. The implementation does not yet support the SAML 2 based Single Sign On (SSO)
       feature of ebXML Registry 3.0 Registry Full Profile.

-----------------------------------freebXML Registry history-------------------------------------

01-August-2007: 3.1

This release contains some new features, some performance enhancements
and a large number of bug fixes.

-New Features

    client and server:

    -Export feature in Web UI
    <http://ebxmlrr.sourceforge.net/wiki/index.php/Run/webui/export>

    client:

    -Visual improvements, improved error handling and reporting.

    server:

    -Parameterized Query invocation via HTTP GET
    <http://ebxmlrr.sourceforge.net/wiki/index.php/Dev/omar/design/httpInterface/paramQueryInvocation>

    -SQL queries now can use any function supported by database

    -An stored AdhocQuery may use any queryLang (query language) for its
    queryExpression

    -Content Filtering Service support
    <http://ebxmlrr.sourceforge.net/wiki/index.php/Dev/omar/design/filteringService>

    -Now supports nested RegistryObjectList within a RegistryPackage and automatically
    creates HasMember Associations between parent package and its nested members.

    -Generated javadoc for entire project:
    <http://ebxmlrr.sourceforge.net/3.0/api/index.html>

    other:

    -New performance optimizations in getClassificationNodeByPath and in AuthenticationServiceImpl 
    
    -New maven.dist target to generate maven2 modules for various omar jars

31-August-2006: 3.0-final1

The "first" final public release conforming to the final ebXML Registry 3.0 specifications that have been
approved as OASIS standard. This release implements the Registry Lite profile completely. It also
implements a near complete support for Registry Full profile. The main missing feature
is the support for SAML Single Sign On (SAML SSO).

It is possible that there may be a 3.0-final2 bug fix release in the near future if necessary.

-New Features

    client and server:

    -Numerous Performance improvements including a new server-side cache

    -Support for specifying an optional query in requests that operate on existing objects
    in registry (approve, deprecate, undeprecate, remove, setStatus etc.) to avoid having to fetch
    the objects just to get their ids, before operating on them 

    client:

    -Numerous usability improvements in Java and Web UI

    -Accessibility improvements in Web UI

    -New "Import" Feature in RegistryBrowser Java UI

    -New "Import" Feature in admintool CLI

    -Support for setStatus extension protocol in Java and Web UI

    -New RegistryFacade interface in JAXR Provider for simplified Java client access

    -New bookmark feature to replace the old PIN feaure in Web UI

    -Added extensions to JAXR Provider for classes defined by ebRIM and not in JAXR.
    These include: Notification, Subscription

    -Added federated query support to Java UI and Web UI

    server:

    -New setStatus extension protocol to allow deployment specific status for RegistryObjects

    -Delivery of Email notification as formatted HTML rather than raw XML

    -Support for secure SMTP protocol in Email Notification

    -Added support for Oracle and MySQL DB

    -Validation and Cataloger plugins

    -Query Plugin feature to add custom query capabilities to the server

    -NotificationListener plugin support for server

    -RequestInterceptor plugin support to allow pre and post interception points for a request

    -Schematron Validation Service for XML documents

    -Improved WSDL Cataloger

    -New XACML Function plugins: AssociationExistsFunction, HasClassificationFunction, HasSlotFunction

    -Ability to specify a custom Access Control Policy (ACP) for an entire objectType rather than only on a per
    instance level. Per instance ACPs override per objectType ACPs.

    other:

    -Added support for Tomcat 5.5.x including DataSource support

    
-Other Improvements

    -Numerous new regression tests in junit test suite (Now at 430 or so)

    -Numerous improvements to Internationalize (I18N) the code base
    
    -Numerous improvements to Localize (L10N) the minDB content

    -Improvement to build.properties mechanism so default properties are set by dev team in build.properties
    and users/deployers may only override select properties in a local.build.properties file as needed.

05-August-2005: 3.0-beta1

The beta public release conforming to the final ebXML Registry 3.0 specifications that have been
approved as OASIS standard.

-New Features

    client and server:

    -Registry Managed Version Control of RegistryObjects and RepositoryItems

    -Added new Person type as specified by ebRIM 3.0

    -ids of objects may now be any user defined URN

    -Made JAXR Provider extensible

    -Made HTTP interface extensible

    -Added Iterative Query feature as specified by ebRS 3.0

    -Added Federated Query feature as specified by ebRS 3.0

    -Added remote object references support as specified by ebRS 3.0

    -Added local replication of remote objects as specified by ebRS 3.0

    -Added XML Filter Query syntax support as specified by ebRS 3.0

    -Alignment with OASIS SOAP Message Security specs as profiled by ebRS 3.0

    -Improved user registration feature to allow CA issued certificates as well
    as improved Registry Issued certificates to be signed by RegistryOperator.

    client:

    -Added publish and user registration features to new Registry Browser Web UI first introduced in 3.0-alpha2

    -Added a new Registry Admin tool which includes ability to change owner of existing objects
    by a User that has RegistryAdministrator role.

    -Removed BulkLoader tool as it is now replaced by "cp" command in admin tool.

    server:

    -Added support for HSQL-DB and Derby databases

    -Now uses RDBMS as repository instead of filesystem as repository

    -Added support for deploying multiple instances of registry server on same server machine

    -Content cataloging and validation services supported either as in-process dynamically loaded
    plugins or as external web services.

    Other:

    -Synced with JWSDP 1.4, JWSDP 1.5 and JWSDP 1.6

    -Added support for profile extensions as exemplified by WSRP and WS profiles in misc/samples/extDB

    -Internationalized the entire code base by moving fixed strings to Resource Bundles.

    -Localized database content for minDB and WS, WSRP Profiles to 7 languages.

    -Numerous performance improvements


-Other Improvements

    client and server:

    -Removed dependency on JAXM API

    -Now propagates exceptions from server to client by marshalling to SOAPFault and then unmarshalling
    back to JAXRException.

    client:

    -JAXR Provider now passes the JAXR TCK

    -Added ability to optionally deploy a RegistryBrowser JNLP (Java WebStart) distribution on deployment server.

    -Accessibility improvements to comply with US Federal Accessibility Standards for Web-based Intranet
    and Internet Information and Applications: http://usability.gov/accessibility/508.html

    -Improved integration with J2EE App Servers by adding support for access to DBMS via JDBC Data Source.

    server:


22-June-2004: 3.0-alpha2

The first public release conforming to the latest ebXML Registry 3.0 specifications. There
are major changes to the registry API. This release is not backward compatible with
2.1-final1. Migration tooling MAY be provided in a subsequent release (resource permitting).

-New Features

    client:

    -Added new Registry Browser Web UI. Instructions on how to use it are at:
     http://ebxmlrr.sourceforge.net/3.0/thinBrowser/UserGuide.html
    -Fat client Registry Browser now fully Internationalized.
     http://ebxmlrr.sourceforge.net/3.0/registryBrowser/
    -Added ability to use drop down combo box based parameter entry for ad hoc query panel using paremeterized
     stored queries in Registry Browser.
    -Now allows JAXR provider to run in same process space as server and bypass
     SOAP calls, signing and verification. See org.freebxml.omar.client.xml.registry.localCall
     property in jaxr-ebxml.properties.
    -Fat client Registry Browser now has multiple themes. The "Large Fonts" theme is useful for demos.
    -Added overloaded JAXR Provider implementation method LifeCycleManagerImpl.saveObjects(HashMap, Collection)
     that allows specifying request Slots as a HashMap. Useful for specifying implementation specific request
     options such as LCM_DO_NOT_COMMIT mode.
    -Added new BulkLoader tool that can load entire file system tree into registry in one command.
    -Added instructions on how to use HTTP/S between clients and the registry:
     http://ebxmlrr.sourceforge.net/3.0/UsingHTTPS.html
    -Added approveObjects() method to LifeCycleManagerImpl to allow client to easily approve objects via
     JAXR API. Requires casting javax.xml.registry.LifeCycleManager to
     org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl
    -Added XACML based access control to the HTTP interface to ebXML Registry.
    -Fat client Registry Browser now has approve, deprecate and undeprecate action menus on objects in Serach Results table.
    -Added ability to log all requests to server made via SOAPMessenger to temporary file. This is useful to
     keep when debugging problems.
    -Added ability to log Submit requests to server made via SOAPMessenger to temporary file. This is useful to
     keep a backup file for submissions so they can be replayed using SOAPSender if rebuilding database.

    server:

    -Consolidated access control based upon XACML. Fixed holes in XACML access control.
     Added support reference access control in XACML policies. You can now control who
     should be allowed to create a reference to your object and under what conditions.
     Uses cases are controlling package membership, classification, ClassificationScheme
     extensions etc. Also added Read access control support.
    -Added Content-based Event Notification feature from V3 specs.
    -Added Parameterized Stored Query feature from V3 specs. Include canonical
     query "Get my User Object".
    -Added support for inter-registry (remote) object references from V3 specs.
    -Added support for auto-creating local replicas of remote objects when they are referenced.
    -UUID generation is exposed as a service by the HTTP interface.
     A request for this service would be made through a URL like:
     http://localhost:8080/omar/registry/http?interface=QueryManager&method=newUUID
    -Performance improvement by caching RegistryGuest, RegistryOperator users instead of
     looking them up each time.
    -Added LCM_DO_NOT_COMMIT mode that allows sending a request to server, server processing the request
     but not actually committing changes at the last stage. This is good for testing purposes in a safe manner.
    -Added support for specifying a different effective owner for the objects being submitted
     if the submitter has RegistryAdministrator role. Similar to setUID in Unix.
    -Added support for a subset of the Object Relocation feature from V3 specs to allow
     a RegistryAdministrator role to reassign ownership of specified objects from current owner
     to a new owner within the same registry.
    -Added better support for other databases such as Oracle 9i, DB2 etc. by generating db specific
     files automatically from common template files.
    -Added access control to HTTP interface.
    -Enhanced HTTP interface with support for file path based URLs derived from RegistryPackage
     membership hierarchy. For example try the following URL or a sub-component:
     http://localhost:8080/omar/registry/http/registry/userData/folder1/folderACP1


-Bugs Fixed

    server:

    -Fixed bug where SELECT * FROM RegistryEntry did not work. Should be backpatched in 2.1-final2.
    -Fixed memory leaks by adding stmt.close() where missing.


-Other Improvements

    -Added numerous unit tests for testing code changes for bug fixes and RFEs.
    -Unified all projects bits under one CVS module named omar and packaged as one package.
    -Split build.xml into various build-*.xml sub-files to manage growing complexity of build files.
    -Replaced Castor with JAXB RI for Java-XML Data Binding. JAXB is a standard Java API.
    -Aligned with Java Web Services Developer Pack 1.3 jars.
    -Allowed specifying proxyHost and proxyPort in build.properties for proxy information.
    -Refactored code to fix inappropriate package dependencies. There are now 3 main code subtrees:
     1. client: Contains client side code, including JAXR API
     2. server: Contains servers side code
     3. common: contains code common to both client and server

    -Fixed so RegistryObject.objectType, RegistryObject.status, RegistryEntry.stability
     and AuditableEvent.eventType use id of ClassificationNodes in corresponding schemes
     instead of enumerated string. This allows these values to be extensible and also support
     inheritance semantics.
    -Improved property file loading. Now supports consistent mechanism for all property files.
     Details at ??.

     server:

    -Migration to version 3.0 SQL Schema and XML Schema
    -Made most major server modules pluggable using a Factory pattern so alternate implementation
     may be used to replace original ones.
    -Increased size of various VARCHAR columns in the database.sql schema


-----------------------------------ebxmlrr-server history-------------------------------------

15-September-2003: 2.1-final1

-New Features

    -HTTP access to RegistryObjects and repository items using Submitter assigned URLs
    -Role base access control using XACML access control policies
    -Enable Case insensitive/sensitive searches in server

-Bugs Fixed

    -BUG 526887 Parent RO id of ExternalIdentifier being saved as null
    -BUG 535553 Table User_ should not have email column
    -BUG 607182 The user id of Administrator should not be hardcoded
    -BUG 711963 Problem creating ServiceBinding
    -BUG 722011 Access URI in ServiceBinding is not saved.
    -BUG 732904 userVersion and status are not saved.
    -BUG 755934 Unable to update User objects
    -BUG 769417 Service Binding Object cannot be saved
    -BUG 771985 Cannot insert Slot(s) for an existing Service

-Other Improvements

    -Redesigned documentation tree based upon Maven
    -RFE 746265 Add more loggings in RegistryJAXMServlet
    -Performance optimization: Eliminate repeated query for RegistryGuest User

06-March-2003: 2.1-beta3

-New Features

    -None

-Bugs Fixed


    -Fixed infinite loop bug during "test.setUCS target in ClassificationNodeDAO"

    -Fixed bug where select on RegistryObject for a User object by id failed to return object

    -Bug fix for bug 672400

    -Bug fix for bugs 666093, 666055, 660799, 666097 (several filter query bugs)

    -Fixed bug where ObjectRef was missing when returning on queries that had composed
     Objects with referenced objects. Result was that Classifications on Classifications
     when unmarshaled in JAXR provider were missing classificationNode attribute.

-Other Improvements

    -Updated build.xml so it uses minimal classpath for compile and runtime

    -Added boolean property ebxmlrr.persistence.rdb.skipAssociationConfirmation which when true
     tells code to skip association confirmation.

    -Replaced println with log statements. Improved message logging.

    -Updated xmlsec.jar tp most recent version 1.0.4

    -Updated log4j.jar to most recent version 1.2.7

    -Update common-logging.jar to most recent version Updated to most recent version 1.0.2

    -Updated canonical ClassificationSchemes based on latest ebXML Registry V3 specification (version 2.35)

    -Updated build.xml to improve database creation target:
     createMinDB: creates minimal database necessary
     createDemoDB: creates a db with most demo data on top of min db
     createMegaDB: create a db with large demo data (e.g. NAICS) on top of demo db.

07-February-2003: 2.1-beta2

-New Features

    -None

-Bugs Fixed

    -Create results directory before running conformance tests.

    -Increased JVM memory thresholds in test.setUCS. The test runs out of memory in some installations otherwise (JDK 1.4.1, Win2k).

    -FilterQueryProcessor now uses platform independent File reference instead of platform dependent URL to link to XSL file.

    -Fixed build.xml and src files so the javacc task will not recompile the file unless the timestamp changes.

    -build.xml now deletes files generated by javacc in clean task.

    -Fixed bug where column named classificationScheme in table Classification was getting incorrectly mapped to classscheme causing query failures.

    -Fixed bug where queries where failing due to column name not getting mapped when there was whitespace leading/trailing the name.


-Other Improvements

    -Updates to FAQ

    -Improved documentation

    -Cleanup of conformance tests

    -SOAPSender no longer supports a default serverURL and requires that it be passed explicitly as argument

    -build.xml now generates build.xml for binary distribution with build_for_bin.xsl

    -Updated documentation in setup.html

    -Removed support for cloudscape because its lack of adequate support for SQL 92

25-July-2002: 2.1-beta1

-New Features

    -Support for REST interface as being defined for OASIS ebXML V3 specifications (ebXML Registry V3 feature)

    -Support for automatic Content Cataloging using default XML Cataloging Service (ebXML Registry V3 feature)

    -Support for Oracle 9 database

-Bugs Fixed

    -Bug fix for 526859 QueryResult format errors
            https://sourceforge.net/tracker/index.php?func=detail&aid=526859&group_id=37074&atid=418900

    -Bug fix for 535666 Unconfirmed Association
            https://sourceforge.net/tracker/index.php?func=detail&aid=535666&group_id=37074&atid=418900

    -Bug fix for 585052 SOAP Sender exits normally on errors
            https://sourceforge.net/tracker/index.php?func=detail&aid=585052&group_id=37074&atid=418900

    -Bug Fix for 550053 Authorization
            https://sourceforge.net/tracker/index.php?func=detail&aid=550053&group_id=37074&atid=418900

    -Bug Fix for 589649 Signature does not verify
            https://sourceforge.net/tracker/index.php?func=detail&aid=589649&group_id=37074&atid=418900

    -Bug Fix for 541005 nested ClassificationNode not updated
            https://sourceforge.net/tracker/index.php?func=detail&aid=541005&group_id=37074&atid=418900

    -Bug Fix for 534586 When ClassificationNode 's code = null
        http://sourceforge.net/tracker/index.php?func=detail&aid=534586&group_id=37074&atid=418900

    -Bug Fix for 593314 no AuditableEvent on Association
        http://sourceforge.net/tracker/index.php?func=detail&aid=593314&group_id=37074&atid=418900

    -Bug Fix for 589649 Signature does not verify
        http://sourceforge.net/tracker/index.php?func=detail&aid=589649&group_id=37074&atid=418900

    -Bug Fix for 526860 Cannot disable logging in xml security
        http://sourceforge.net/tracker/index.php?func=detail&aid=526860&group_id=37074&atid=418900

    -Bug Fix for 538289 Canned attachments dsig incorrect format
        http://sourceforge.net/tracker/index.php?func=detail&aid=538289&group_id=37074&atid=418900

    -Bug Fix for 539203 not delete payload signature
        http://sourceforge.net/tracker/index.php?func=detail&aid=539203&group_id=37074&atid=418900

    -Bug Fix for 539879 Concept.getPath() not efficient
        http://sourceforge.net/tracker/index.php?func=detail&aid=539879&group_id=37074&atid=418900

    -Bug Fix for 544023 == test in AuthorizationServiceImpl
        http://sourceforge.net/tracker/index.php?func=detail&aid=544023&group_id=37074&atid=418900

    -Bug Fix for 592960 Too many cursors
        http://sourceforge.net/tracker/index.php?func=detail&aid=592960&group_id=37074&atid=418900

    -Bug Fix for 627995 Need ability to configure trust anchors
        http://sourceforge.net/tracker/index.php?func=detail&aid=627995&group_id=37074&atid=418900

-Other Improvements

    -Improved documentation
        -Include section on how to control logging including logging of XML security
        -Include section on what we have implemented that are not in the spec, or implement differently:
        - Auto user registration
        - Allow updating objects with SubmitObjectsRequest
        - Repository Quota
        - Unsigned registry response

    -Improved installation process

    -Improved performance

    -Included ebxmlrr-server license

    -Included licenses for 3rd party software

-----------------------------------ebxmlrr-client history-------------------------------------

15-September-2003: ebxmlrr-client2.1-final1

-New Features

    Registry Browser:

    -Added Locale Selection dialog
    -RFE 782375 Now shows value from closest locale using InternationalString.getClosestValue
    -Added InternationalStringPanel to allow editing strings in any locale.
    -RFE 741398	Ability to configure search result output
    -RFE 783003	SQL filter queries should be case insensitive
    -RFE 783137	Columns in Search Results should be sortable
    -RFE 783014	Enable configuration of displayed ClassificationSchemes
    -RFE 785877 Add support for readonly mode when user is unauthenticated
    -RFE 83128 Support auto-connect of specified Registry Location URL
    -RFE 796601	Integrate web browser with registry browser
    -RFE 796604	Provide confirmation on remove actions
    -RFE 804135	Need ENTER key in dialogs to activate default button

    JAXR Provider:

-Bugs Fixed

    Registry Browser:
    -BUG 719771 Add ServiceBinding dialog should be centered
    -BUG 732918 Exception while Show Related Objects on SpecificationLink
    -BUG 740582 Browser exits on bad registry URL
    -BUG 740689 Can't add/modify/delete Classification for existing Service
    -BUG 745795 Email address is not saved in User Registration Wizard
    -BUG 771399 should not allow user to enter extra data than DB allows
    -BUG 771916 Browser Hanging after edited Classification entry in Service (Mei??)
    -BUG 771973 Empty External URL field cause GUI display problem
    -BUG 781191 ShowRelatedObjects: Hard to see all objects due to layout
    -BUG 769413 Classification Scheme Labels not shown in locale "en"
    -BUG 781208 Related Serv. Binding in graph panel missing classification
    -Bug 787040: JGraph shows popup menu inconsistently
    -Bug 740746	Classification box incorrectly re-sized
    -Bug 787798 Error in Show Audit Trail if User no longer in registry
    -Bug 786911	Key remains in keystore after authentication fails
    -Bug 786878	Do not null in Service Dialog for empty classification names
    -BUG Fix charset to utf-8 when encoding chars into bytes that will be fed to a XML Parser.
    -BUG User data entered should not be cleared on exception.

    JAXR Provider:

    -BUG 692195 JAXR provider must not depend on client packages
    -BUG 707508 LocalizedStrings lost for non-default Locales
    -BUG 752850 ebxml/util package in two jar files
    -BUG 755443 Updates to Service object result in an insert
    -BUG 755450 Security-related classes/properties go into jaxr-ebxml.jar
    -BUG 769356 Service Object's ExternalLink duplicates for each save
    -BUG 776712 ExtensibleObjects not flagged as modified for Slot changes
    -BUG 779968 unable to store browser config error message

-Other Improvements

    Registry Browser:

    -RFE 776679  Add option to pre-expand Object Type combo box
    -RFE 777221 Need to cache ClassificationSchemes for performance
    -More validation when creating creating/editing objects in RegistryBrowser
    -Configurable object type to display as initial selection in RegistryBrowser ObjectType combo
    -RFE 787775	Need a more obvious Find button in Discovery Panel

    JAXR Provider:

06-March-2003: 2.1-beta3

Registry Browser Improvements:

    -Now allows a custom icon for user defined objectTypes usd by ExtrinsicObjects.
     Simply place image file in resources directory with filename prefix matching the
     objectType (e.g. CPPA.gif).

    -Circle layout in SHow Related Objects when graphical browsing now
     has upper limit on radius to avoid unwieldy graphs.

    -ClassificationScheme viewer now sorts children alphanumerically based on the code attribute.

    -Fixed bugs in set/add/removeExternalLink(s) methods in RegistryObject

    -Removed Specification Concepts from FindParamsPanel as it is not relevant to ebXML.

    -Design improvements in User Registration (now using MVC model)

JAXR Provider Improvements:

    -Added set/getSource/TargetObjectRef in AssociationImpl

    -Numerous bug fixes in LifecycleManagerImpl.saveObjects
     Unmodified objects were being saved. Composed objects were
     being saved multiple times, modified objects were not being saved.

    -Fixed bug where addSlot(s) and removeSlot(s) did not mark the object as modified.

    -Fixed bug where objects were disappearing from Object cache due to GC

    -Fixed bug where Classifications on Classification were not being returned

    -Design improvements in authentication

Other Improvements:

    -Updated build.xml so it uses minimal classpath for compile and runtime

    -Replaced println with log statements. Improved message logging.

    -Automated junit tests with targets run.junit.tests and run.junit.reports

    -Removed unused jars

07-February-2003: 2.1-beta2

Registry Browser Improvements:

    -Fixes bug where user could type some keystore password in user registration and authentication dialogs that was different from the keystore password in jaxr-ebxml.properties file.

    -Fixes bug were authentication dialog could be hidden under main RegistryBrowser frame and cause app to lock up

    -Added new "Find specified Organization's Objects" ad hoc query.

    -Added defaultValues to existing ad hoc queries.


JAXR Provider Improvements:

    -Fixed platform specific code in file paths


Other Improvements:

    -None

25-July-2002: 2.1-beta1

-Registry Browser Improvements

    -User Registration now works well. The only catch is that keystore password must be entered as ebxmlrr (or whatever you have in
    <user.home>/ebxmlrr.properties).

    -Automatically creates a <user.home>/.java.login.config file JAAS LoginModule configuration if needed. This is totally transparent to the user now.

    -The ClassificationScheme/Concept dialog (2nd toolbar icon from left) now allows creating, editing, deleting schemes and concepts using right
    mouse click enable popup menu.

    -Improved ability to graphically associate objects in Graphical panel (submission panel, browse panel) using new Relationship dialog.

    -Shows busy cursor in all time consuming operations.

    -Graph Panel does now allows saving multiple selected objects.

    -Support for ExtrinsicObjects and submission of arbitrary repository items to the ebXML registry.

    -Support for viewing audit trail on a registry object.

    -Support for all types of RegistryObjects in Find Parameter's Object Type drop down list and Submission panel Object Type drop down list.

-JAXR Provider Improvements

    -JAXR compliance i.e. Changes to pass the JAXR Conformance Test Suite (CTS)

    -Bug fix for 589318  JAXR provider requires SQLQuery support
            https://sourceforge.net/tracker/?func=detail&aid=589318&group_id=37074&atid=418900

-Other Improvements

    -Improved documentation
            -Include description of how to configure proxy information when behind a firewall.

    -Improved installation process

    -Improved performance


