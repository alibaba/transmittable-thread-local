#!/bin/bash
set -eEuo pipefail

dirs=(
    2.0.0
    2.0.1
    2.0.2
    2.1.0
    2.1.1
    2.2.0
    2.2.1
    2.2.2
    2.3.0
    2.3.1
    2.4.0
    2.5.0
    2.5.1
    2.6.0
    2.6.1
    2.7.0
    2.8.0
    2.8.1
    2.9.0
    2.10.0
    2.10.1
    2.10.2
    2.11.0
    2.11.0-RC1
    2.11.0-RC2
    2.11.0-RC3
    2.11.1
    2.11.2
    2.11.3
    2.11.4
    2.11.5
    2.12.0
    2.12.1
    2.12.2
    2.12.3
    2.12.3-RC1
    2.12.4
    2.12.5
    2.12.6
    2.13.0
    2.13.0-Beta1
    2.13.1
    2.13.2
    2.14.0
    2.14.1
)


for d in "${dirs[@]}"; do
    rm -rf "$d"
    mkdir "$d"

    cat  > "$d/index.html" <<EOF
<html>
	<head>
		<script language="javascript" type="text/javascript">
			window.location.href="https://javadoc.io/doc/com.alibaba/transmittable-thread-local/$d/index.html"
		</script>
	</head>
	<body></body>
</html>
EOF

done
