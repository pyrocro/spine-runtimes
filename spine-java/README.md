# spine-java

The spine-java runtime provides functionality to load and manipulate [Spine](http://esotericsoftware.com) skeletal animation data using Java. It does not perform rendering but can be extended to enable Spine animations for other Java-based projects.
This project is basically a Java-based conversion of the [spine-csharp](https://github.com/mbarbeaux/spine-runtimes/tree/master/spine-csharp) project.
Note that it is not used for the [spine-libgdx](https://github.com/mbarbeaux/spine-runtimes/tree/master/spine-libgdx) project.

## Setup

1. Download the Spine Runtimes source using [git](https://help.github.com/articles/set-up-git) or by downloading it [as a zip](https://github.com/EsotericSoftware/spine-runtimes/archive/master.zip).
2. Using Eclipse, import the project by choosing File -> Import -> Existing Maven projects. For other IDEs, you need to check if they manage Maven projects, else you will need to create a new project and import the source.

Alternatively, the contents of the `spine-java/src` directory can be copied into your project.

## Runtimes Extending spine-java

- [spine-playn](https://github.com/mbarbeaux/spine-runtimes/tree/master/spine-playn)
