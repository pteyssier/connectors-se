#! /bin/bash

#
#  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


. "$(dirname $0)/common.sh" $1
export DOCKER_HTML="$BASEDIR/target/docker.html"

mkdir -p target
echo > "$DOCKER_HTML" << EOF
<!DOCTYPE html>
<html>
<head>
<style>
table, th, td {
  border: 1px solid black;
  border-collapse: collapse;
}
</style>
</head>
<body>

<h2>Docker Images</h2>

<table>
  <tr>
    <th>Repository</th>
    <th>Image</th>
    <th>Version</th>
    <th>Tag</th>
  </tr>
EOF


. "$(dirname $0)/repository.sh"
. "$(dirname $0)/server.sh"

echo >> "$DOCKER_HTML" << EOF
</table>

</body>
</html>
EOF

