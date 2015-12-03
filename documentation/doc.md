Installation
------------

Here is how to get up and running with the hub.

Download the hub:

    {{ download-hub.sh|content }}

Which should output something like this:

    {{ download-hub.sh|bash }}

Download the frontend::

    {{ download-frontend.sh|content }}

Resulting in:

    {{ download-frontend.sh|bash }}

Unzip the hub::

    {{ unzip-hub.sh|content }}

Resulting in:

    {{ unzip-hub.sh|bash }}

Unzip the frontend::

    {{ unzip-frontend.sh|content }}

Resulting in:

    {{ unzip-frontend.sh|bash }}

Run the hub::

    {{ run-hub.sh|content }}

Resulting in:

    {{ run-hub.sh | bash[killAfter='10 seconds'] }}

Run the frontend:

    {{ run-frontend.sh | content }}

Resulting in:

    {{ run-frontend.sh | bash[killAfter='10 seconds'] }}

