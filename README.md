[AutoDeriv](http://nodj.github.io/AutoDeriv)
============================================

##### eclipse plug-in that handle the 'derived' state of resources from a textual file.

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

For more information, examples of use, see the [home page](http://nodj.github.io/AutoDeriv)

If something is wrong, or if you have an idea, a proposition, you should open an [issue](https://github.com/nodj/AutoDeriv/issues).

If this plugin helped you, support it back with a [donation](https://pledgie.com/campaigns/27750)!
