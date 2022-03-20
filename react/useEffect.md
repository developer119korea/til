컴포넌트가 언마운트 될때 처리해야할 일이 있다면 useEffect에서 콜백 함수를 리턴하면된다.

```
  useEffect(() => {
    if (!userId) {
      return;
    }
    const stopSync = cardRepository.syncCards(userId, cards => {
      setCards(cards);
    });
    return () => stopSync();
  })
 ```