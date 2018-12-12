clj-dynamodb-local [![Build Status](https://travis-ci.org/dmcgillen/clj-dynamodb-local.svg)](https://travis-ci.org/dmcgillen/clj-dynamodb-local)
===================

A Leiningen 2 plugin and boot task providing a local DynamoDB instance to run tests against.

This starts an instance of DynamoDB Local (http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html) provided by Amazon and shuts it down once any proceeding tasks have completed. DynamoDB Local is downloaded on first run, and stored in ~/.clj-dynamodb-local.
## Usage

#### Leiningen

Add the following to the `:plugins` vector of your project.clj:

[![Clojars Project](http://clojars.org/clj-dynamodb-local/latest-version.svg)](http://clojars.org/clj-dynamodb-local)

You can start DynamoDB Local, followed by any other tasks you may want to run, using the following:

    $ lein dynamodb-local other-tasks...

The plugin will shutdown DynamoDB Local upon completion.

DynamoDB Local can be started without running any other tasks if you so wish:

    $ lein dynamodb-local

Killing the process (e.g. with <kbd>Ctrl</kbd>+<kbd>C</kbd>) will shutdown DynamoDB Local.

If run as a background process (e.g. `$ lein dynamodb-local &`) all child processes should be terminated upon completion (e.g. `pkill -P parent-process-id`). The plugin will handle the termination of the DynamoDB Local process.

#### Boot

Add this library as a dependency in your build.boot file:

[![Clojars Project](http://clojars.org/clj-dynamodb-local/latest-version.svg)](http://clojars.org/clj-dynamodb-local)

`require` the boot namespace and task provided in the appropriate place in your build.boot:

`'[dynamodb-local.boot :refer [dynamodb-local]]`

Add the `dynamodb-local` boot task into your build pipeline at the appropriate point supplying options [#](see below) as necessary, for example:

```clojure
(deftask development []
  (comp (testing)
        (dynamodb-local :project {:dynamodb-local {:port 8090
                                                   :in-memory? true}})
        (repl)))
```

A shutdown hook will be added to shutdown DynamoDB Local when teh JVM exists.

### Configuration

You can set some parameters by adding the `:dynamodb-local` keyword to your project.clj.

#### :port

Set the port that you want DynamoDB Local to run on (defaults to 8000).

```clojure
:dynamodb-local {:port 12345}
```

It is also possible to set the port by using the environment variable `DYNAMODB_PORT`.

#### :in-memory?

Set if you want DynamoDB Local to be run in memory (defaults to false).

Note: This should not be used in conjunction with the `:db-path` parameter. If it is, `:db-path` will be ignored.

```clojure
:dynamodb-local {:in-memory? true}
```

#### :shared-db?

Set if you want DynamoDB Local use a shared db rather than have separate files for each region/credentials combo. See the `-sharedDb`
switch documented [here](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html) for more info.

```clojure
:dynamodb-local {:shared-db? true}
```

#### :db-path

Set the path that DynamoDB Local will write its database file to (defaults to ./.clj-dynamodb-local). If a relative path is used, it will be relative to the current directory.

Note: This should not be used in conjunction with the `:in-memory?` parameter. If it is, `:db-path` will be ignored.

```clojure
:dynamodb-local {:db-path "/path/to/desired/db/output/"}
```

#### :jvm-opts

Pass JVM options to the DynamoDB process. Behaves the same as the 'normal' :jvm-opts in a project.clj

```clojure
:dynamodb-local {:jvm-opts ["-server" "-Xmx512m"]}
```

## License

Copyright © 2018 Donovan McGillen

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
