package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import org.springframework.stereotype.Service;

@Service
public class OCRService {

    public void extractContent(Document document) {
        // Mock OCR
        // 실제로는 AWS Textract 등이 PDF(S3 Url)를 읽어서 아래와 같은 긴 텍스트(=rawContent)를 반환
        String mockContent = """
                [1강: Java 기초와 객체지향 프로그래밍]

                1. 자바(Java) 언어의 개요
                자바는 1995년 썬 마이크로시스템즈에서 발표한 객체지향 프로그래밍 언어입니다.
                "Write Once, Run Anywhere"라는 슬로건처럼, 운영체제에 독립적으로 실행될 수 있다는 강력한 장점이 있습니다.
                이것이 가능한 이유는 바로 JVM(Java Virtual Machine) 덕분입니다.

                2. 객체지향 프로그래밍(OOP)의 4대 특징
                객체지향 프로그래밍은 프로그램을 수많은 '객체(Object)'라는 기본 단위로 나누고 이들의 상호작용으로 서술하는 방식입니다.

                (1) 캡슐화 (Encapsulation)
                 - 데이터(속성)와 그 데이터를 처리하는 코드(메서드)를 하나로 묶는 것을 말합니다.
                 - 정보 은닉(Information Hiding): 객체 내부의 상세한 구현 내용을 외부로부터 감추어, 잘못된 접근으로 인한 오류를 방지합니다.
                 - 접근 제어자(private, protected, public)를 통해 구현됩니다.

                (2) 상속 (Inheritance)
                 - 부모 클래스(Super Class)의 특징(필드, 메서드)을 자식 클래스(Sub Class)가 물려받는 것입니다.
                 - 코드의 재사용성을 높이고, 유지보수 시간을 단축시켜 줍니다.
                 - 자바에서는 'extends' 키워드를 사용하며, 다중 상속은 지원하지 않습니다(인터페이스 제외).

                (3) 다형성 (Polymorphism)
                 - 하나의 변수명, 함수명 등이 상황에 따라 다른 의미로 해석될 수 있는 성질입니다.
                 - 오버로딩(Overloading): 같은 이름의 메서드를 매개변수의 유형과 개수를 다르게 하여 여러 개 정의하는 것.
                 - 오버라이딩(Overriding): 상위 클래스가 가지고 있는 메서드를 하위 클래스가 재정의하여 사용하는 것.

                (4) 추상화 (Abstraction)
                 - 불필요한 정보는 숨기고 중요한 정보만을 표현함으로써 프로그램을 간단하게 만드는 것입니다.
                 - 추상 클래스(Abstract Class)와 인터페이스(Interface)가 대표적인 예입니다.

                3. JVM 메모리 구조
                JVM은 OS로부터 메모리를 할당받아 자바 프로그램을 실행합니다. 주요 영역은 다음과 같습니다.
                 - Method Area: 클래스 정보, static 변수 등이 저장됩니다.
                 - Heap Area: keyword 'new'로 생성된 객체와 배열이 생성되는 영역입니다. GC(Garbage Collector)의 주 대상입니다.
                 - Stack Area: 메서드 호출 시 생성되는 지역 변수, 매개 변수 등이 저장됩니다.
                """;
        document.updateRawContent(mockContent);
    }
}
