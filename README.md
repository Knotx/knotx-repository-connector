[![Build Status](https://dev.azure.com/knotx/Knotx/_apis/build/status/Knotx.knotx-repository-connector?branchName=master)](https://dev.azure.com/knotx/Knotx/_build/latest?definitionId=9&branchName=master)
[![CodeFactor](https://www.codefactor.io/repository/github/knotx/knotx-repository-connector/badge)](https://www.codefactor.io/repository/github/knotx/knotx-repository-connector)
[![codecov](https://codecov.io/gh/Knotx/knotx-repository-connector/branch/master/graph/badge.svg)](https://codecov.io/gh/Knotx/knotx-repository-connector)


# Repository Connector
Repository Connector is a [**Handler**](https://github.com/Knotx/knotx-server-http/tree/master/api#routing-handlers)
that enables fetching the content to process during [HTTP Server request processing](https://github.com/Knotx/knotx-server-http#how-does-it-work) 
from the content repository (like headless or traditional CMS or simply a filesystem location).

## How does it work
Repositories are not part of Knot.x itself. These are the stores of documents, 
e.g. CMS systems, HTTP servers, filesystem locations, databases or any other 
systems that can deliver content. 

## How to use
Simply configure a connector as the [Server Routing Operation Handler](https://github.com/Knotx/knotx-server-http#routing-operations),
e.g. 
```hocon
routingOperations = [
  {
    operationId = my-operation
    handlers = [
      // other handlers
      {
        name = httpRepoConnectorHandler
        config = {
          // connector configuration
        }
      }
      // other handlers
    ]
  }
]

```

## Available connectors
This repository contains two implementations of connectors that you may read about in details inside
the modules:
- [HTTP Repository Connector](https://github.com/Knotx/knotx-repository-connector/tree/master/http)
- [Filesystem Repository Connector](https://github.com/Knotx/knotx-repository-connector/tree/master/fs)
