# HaJenkins

You need the following ingredients 

 1. DbBacked builds ( https://github.com/groupon/DotCi ) , i.e no in memory build data.  
 2. NFS mount for 'jobs' folder to share console output, artifacts ect. 
 3. Message Passing between various masters ( eg: build queuing, build abort, queue abort ect ) . https://github.com/suryagaddipati/HaJenkins
 4. No shared agents. You could use any cloud plugin , although I like instant provisioning ones eg: https://github.com/suryagaddipati/jenkins-docker-swarm-plugin


Setup: 

 1. Install and configure plugins mentioned above
 2. Start multiple Jenkins instances with share mounted/symlinked  'jobs' folder . 
 3. Done. 


Method: 

 * Build Create: When a build comes into the queue, it gets saved into redis queue ( I had to hack override Queue Implementation to achieve this).  Build gets picked up any of the masters that are watching the queue. 

* Build Abort:  Aborting a 'ha build' puts a message into redis queue , which gets processed by all masters and the master running the actual build aborts it. 
 * Queued item Abort: Same mechanism as above ^. 
 * Build Execution: Build execution happens in a dynamically created agent.  Build info is written to db via DotCi, and build logs are synced to all masters via NFS. 
 
 * Build Delete: Build gets deleted in DotCi database. 




I've done some testing and this setup seems to work fine and is totally transparent to end user.  But I haven't done any extensive testing for edge cases.
