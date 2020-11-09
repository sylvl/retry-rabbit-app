Demo application featuring RabbitMq, Spring Cloud Stream and Spring Amqp

The app does nothing useful obviously :-) 
* The app checks if the payload of the input message contains the word "retry", 
* if so, the app sends the message to a _retry-exchange_.
* Depending on the number of retries, the message will be routed to a waiting queue (1 second and 10 seconds), or a parking queue
* Before sending the message to the parking queue, the app will send a message to a "log queue" with `StreamBridge`

Implementation notes:
* The retry-mechanism is based on the per-queue message TTL and on dead letter exchanges
* Spring Amqp is used to set up RabbitMq queues and exchanges (Cloud Stream doesn't allow fine-grained RabbitMq configuration)
* Cloud Stream : 
  - use of functional programming model
  - dynamic routing
  - use of `StreamBridge`
* Some tests use Spring Cloud test binder, and others are run only if a rabbit mq server is running on localhost    


Notes on the retry-infrastructure (queues and exchanges):
* it can be shared across multiple applications
* adding more wait queues is easy (only requires adding a couple of properties)
* it should be easy to extract the retry features, and include them into a library 
