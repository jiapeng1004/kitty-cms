"""
统计信息API
"""
from flask import jsonify
from datetime import datetime, date
from src.api.v1 import api_v1
from src.models import Sentiment, Source, Task

@api_v1.route('/stats', methods=['GET'])
def get_stats():
    """获取仪表盘统计信息"""
    from src.main import app
    db = app.session_factory()
    
    try:
        # 舆情总数
        total_sentiments = db.query(Sentiment).count()
        
        # 今日新增舆情
        today = date.today()
        today_sentiments = db.query(Sentiment).filter(
            Sentiment.publish_time >= datetime.combine(today, datetime.min.time()),
            Sentiment.publish_time <= datetime.combine(today, datetime.max.time())
        ).count()
        
        # 监控源数量
        active_sources = db.query(Source).count()
        
        # 运行中的任务数量
        running_tasks = db.query(Task).filter(Task.status == 'running').count()
        
        return jsonify({
            "totalSentiments": total_sentiments,
            "todaySentiments": today_sentiments,
            "activeSources": active_sources,
            "runningTasks": running_tasks,
        })
    finally:
        db.close()
