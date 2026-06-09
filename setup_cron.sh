#!/bin/bash
(crontab -l 2>/dev/null; echo "*/5 * * * * cd /home/ubuntu/AlmacenApp && /home/ubuntu/AlmacenApp/ai_env/bin/python3 scripts/bg_worker.py >> /home/ubuntu/AlmacenApp/bg_worker.log 2>&1") | crontab -
