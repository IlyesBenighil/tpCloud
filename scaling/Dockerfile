FROM python:2

WORKDIR /app
RUN pip install delegator.py
RUN pip install web.py
ADD pdfmagic/pdfmagic.py /app
EXPOSE 8080

CMD ["python", "/app/pdfmagic.py"]