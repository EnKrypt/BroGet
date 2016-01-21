BroGet
==
[![Build Status](https://travis-ci.org/EnKrypt/BroGet.svg)](https://travis-ci.org/EnKrypt/BroGet)

#### *BroGet is content distribution on steroids.*

It is a remote solution managed by the cloud to trigger downloads across networks and have those files retrieved at the right place, by the right person.

==
### Working :

The Android App or similarly compatible client sends a request to the tracker in the cloud that contains information about :
* The download link
* The download box IDs that will fetch that file in that link
* The client IDs that are authorized to recieve those files

The cloud then stores this data in its database. 
After a regular interval, when the download box polls the tracker and there is a box ID match, it receives the information in the original request. It then :
* Downloads the file at the download link to the box's internal storage
* Stores the request in the box's database

Then, when a compatible client enters a network that contains a download box, a request is sent to it and the following happens :
* If there is a client ID match, respective files stored in the box is pushed to the client
* The client ID is now removed from the list in the original request in the box as well as the tracker to prevent duplicate downloads
* Other download boxes in different networks are also updated with this change so there will not be a second download when the client switches network

When the list of client IDs in a request becomes empty, the request is cleared and the associated file is deleted. This is mirrored across all the boxes. The request is also cleared in the tracker.

==
### Application :

//TODO : Fill these out