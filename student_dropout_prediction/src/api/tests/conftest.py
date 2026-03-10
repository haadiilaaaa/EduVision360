import pytest

@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()

    if report.when == "call":
        title = item.function.__doc__.strip() if item.function.__doc__ else item.name
        status = "PASSED" if report.passed else "FAILED" if report.failed else "SKIPPED"
        print(f"\n{title} --> {status}")