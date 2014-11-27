AutoDeriv
=========

eclipse plug-in that handle the 'derived' state of resources from a textual file.

The goal is to manage derived resources in a similar way than .gitignore or svn ignore.
A file based filtering allows:
* easy sharing of the configuration
* no issue with automatic generation
* no issue with deletion-recreation cycles
* readable, centralized, easy configuration

The plug-in aims to make all this totally transparent for the end user.
It should:
* auto-detect which project of the workspace uses this functionality or not
* perform update as fast as possible without slowing down the IDE or reducing UX
* not require additional, in-IDE configuration (except plug-in installation of course)
