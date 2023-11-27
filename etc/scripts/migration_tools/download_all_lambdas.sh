#This script doesn't work (fix the curl somehow)

mkdir code
aws lambda list-functions --profile siryus | \
grep FunctionName | \
cut -d '"' -f4 | \
while read -r name; do
	aws lambda get-function --profile siryus --function-name $name | ./jq-win64.exe -r '.Code.Location' | xargs -I {} sh -c "curl -o curl -o NUL "${url1%?}" {}; ls -la {} | tail -2"
done

    