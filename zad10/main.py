from collections import defaultdict
from typing import List
import numpy as np
import matplotlib.pyplot as plt
import datetime

SESSION_TIME = 60 * 60  # 3h
PAGE_TIME = 15 * 60  # 10min
PAGES = ["Shop.html", "AddProduct.html", "Summary.html", "Payment.html", "About.html", "Contact.html"]
LINKS = [[1, 4], [2, 4], [3], [], [1, 5], []]


class Request:
    def __init__(self, line):
        splitted = line.split(' ')
        self.ip = splitted[0]
        # convert hours + minutes + seconds -> total number of seconds
        rtime = [int(x) for x in splitted[1].split('/')[-1].split(':')[1:]]
        self.time = rtime[0] * 3600 + rtime[1] * 60 + rtime[2]
        self.page = splitted[4]

    def __repr__(self):
        return self.page


class Session:
    def __init__(self, requests: List[Request]):
        self.startTime = requests[0].time
        self.stopTime = requests[-1].time
        self.requests = requests

    def __repr__(self):
        return f'{str(datetime.timedelta(seconds=self.startTime))} -> {str(datetime.timedelta(seconds=self.stopTime))},' \
               f' {len(self.requests)}'


class Computer:
    def __init__(self, ip, requests: List[Request]):
        self.ip = ip
        self.sessions = []
        self._set_sessions(requests)

    @staticmethod
    def _time_exceeded(session: List[Request], current: Request):
        return (current.time - session[-1].time) > PAGE_TIME or (current.time - session[0].time) > SESSION_TIME

    @staticmethod
    def _link_exists(session: List[Request], current: Request):
        return current.page == session[-1].page or PAGES.index(current.page) in LINKS[PAGES.index(session[-1].page)]

    def _set_sessions(self, requests: List[Request]):
        current_session = [requests[0]]
        for request in requests[1:]:
            if self._time_exceeded(current_session, request) or not self._link_exists(current_session, request):  # session finito
                new_session = Session(current_session)
                self.sessions.append(new_session)
                current_session = [request]
            else:
                current_session.append(request)
        new_session = Session(current_session)
        self.sessions.append(new_session)


def find_by_ip(computers, ip):
    for computer in computers:
        if computer.ip == ip:
            return computer
    return None


file = open("log.txt", "r")
lines = file.readlines()
for l in range(3): print(lines[l][:-1])

reqs = [Request(line) for line in lines]

computers = []
sessions = []

reqs_by_ip = defaultdict(list)
for request in reqs:
    reqs_by_ip[request.ip].append(request)

for ip, requests in reqs_by_ip.items():
    new_computer = Computer(ip, requests)
    sessions += new_computer.sessions
    computers.append(new_computer)

exercise = lambda e: print(f'{"="*10}{e}{"="*10}')

exercise('3.1')  # 1000
print(sum(len(x.sessions) for x in computers))

exercise('3.2')  # 391
print(len(computers))

exercise('3.3')  # 2.557544757033248
print(sum(len(x.sessions) for x in computers) / len(computers))

# exercise('3.4')
# sessions_len_minutes = [(x.stopTime - x.startTime) // 60 for x in sessions]
# plt.hist(sessions_len_minutes, 50, alpha=0.5)
# plt.plot()
# plt.show()


# exercise('3.5')
# sessions_len_requests = [len(x.requests) for x in sessions]
# plt.hist(sessions_len_requests, 20, alpha=0.5)
# plt.plot()
# plt.show()

# exercise('3.6')
# sessions_start_hour= [x.startTime // 3600 for x in sessions]
# plt.hist(sessions_start_hour, 24, alpha=0.5)
# plt.plot()
# plt.show()

# exercise('3.7')
# entrypoints = [0] * len(PAGES)
# for session in sessions:
#     index = PAGES.index(session.requests[0].page)
#     entrypoints[index] += 1
# plt.bar(PAGES, entrypoints)
# plt.xticks(rotation=45)
# plt.plot()
# plt.show()

# exercise('3.8')
# departures = [0] * len(PAGES)
# for session in sessions:
#     index = PAGES.index(session.requests[-1].page)
#     departures[index] += 1
# plt.bar(PAGES, departures)
# plt.xticks(rotation=45)
# plt.plot()
# plt.show()

exercise('3.9')
temp = {page: {'time': 0, 'hits': 0} for page in PAGES}
for computer in computers:
    for session in computer.sessions:
        for i in range(len(session.requests) - 1):
            temp[session.requests[i].page]['time'] += session.requests[i + 1].time - session.requests[i].time
            temp[session.requests[i].page]['hits'] += 1

avg_time = [v['time']/v['hits'] if v['hits'] else 0 for v in temp.values()]
plt.bar(PAGES, avg_time)
plt.xticks(rotation=45)
plt.plot()
plt.show()

exercise('3.10')
for session in sessions[:10]:
    print(*session.requests)